package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Captcha;
import cn.tgozzz.legal.utils.Sms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Optional;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
@Slf4j
public class SmsHandler {

    @Resource
    private ReactiveRedisTemplate<String, Captcha>  reactiveRedisTemplate;

    /**
     * 核对验证码
     */
    public Mono<ServerResponse> checkCode(ServerRequest request) {

        // 获取手机号
        String phone = request.pathVariable("phone");
        // 验证码
        String code = request.queryParam("code").get();
        // 获取Redis操作接口
        ReactiveValueOperations<String, Captcha> operations = reactiveRedisTemplate.opsForValue();

        return operations
                // 根据路径参数 phone 获取验证码
                .get(request.pathVariable("phone"))
                // 核验
                .map(captcha -> captcha.getCode())
                .flatMap(res -> code.equals(res) ?
                        ok().contentType(TEXT_PLAIN).bodyValue("验证码正确") :
                        badRequest().contentType(TEXT_PLAIN).bodyValue("验证码错误"))
                .switchIfEmpty(notFound().build());
    }

    /**
     * 验证码校验直接通过
     */
    public Mono<ServerResponse> noCheckCode(ServerRequest request) {
        return ok().build();
    }

    /**
     * 查询验证码
     */
    public Mono<ServerResponse> getCode(ServerRequest request) {

        ReactiveValueOperations<String, Captcha> operations = reactiveRedisTemplate.opsForValue();

        return operations
                // 根据路径参数 phone 获取验证码
                .get(request.pathVariable("phone"))
                .flatMap(captcha -> ok().contentType(TEXT_PLAIN).bodyValue(captcha.toString()))
                .switchIfEmpty(notFound().build());
    }

    /**
     * 发送验证码
     */
    public Mono<ServerResponse> setCode(ServerRequest request) {

        // 获取手机号
        String phone = request.pathVariable("phone");
        // 生成验证码
        String code = String.valueOf((int) (Math.random() * (99999 - 10000 + 1) + 10000));
        // 模板短信
        String template = "【法务小助手】您的验证码是" + code + ",５分钟内有效。若非本人操作请忽略此消息。";
        // 获取Redis操作接口
        ReactiveValueOperations<String, Captcha> operations = reactiveRedisTemplate.opsForValue();

        return new Sms()
                .setPhone(phone)
                .setContent(template)
                .send()
                .filter(Boolean::booleanValue)
                .flatMap(res -> operations.set(phone, new Captcha(phone, code), Duration.ofMinutes(5)))
                .filter(Boolean::booleanValue)
                .flatMap(res -> ok().bodyValue("发送成功"))
                .switchIfEmpty(status(500).bodyValue("验证码发送失败"));
    }
}
