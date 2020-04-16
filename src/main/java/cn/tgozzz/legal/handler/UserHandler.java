package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Captcha;
import cn.tgozzz.legal.domain.Token;
import cn.tgozzz.legal.domain.User;
import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Log4j2
@Component
public class UserHandler {

    private final UserRepository repository;

    private final ReactiveRedisTemplate reactiveRedisTemplate;

    public UserHandler(UserRepository repository, ReactiveRedisTemplate reactiveRedisTemplate) {

        this.repository = repository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    /**
     * 获取用户列表
     */
    public Mono<ServerResponse> listPeople(ServerRequest request) {
        log.info("listPeople");
        return repository
                .findAll().collectList()
                .flatMap(res -> ok().bodyValue(res));
    }

    /**
     * 批量添加用户
     */
    public Mono<ServerResponse> createPeople(ServerRequest request) {
        log.info("createPeople");
        return request
                .bodyToFlux(User.class)
                .doOnNext(user -> user.setUid(null))
                .flatMap(repository::save)
                .collectList()
                .flatMap(list -> ok().bodyValue(list));
    }

    /**
     * 删除所有用户
     */
    public Mono<ServerResponse> deletePeople(ServerRequest request) {
        log.info("deletePeople");
        return repository
                .deleteAll()
                .flatMap(res -> ok().bodyValue(res));
    }

    /**
     * 通过验证码添加用户
     */
    public Mono<ServerResponse> addUserByCaptcha(ServerRequest request) {
        log.info("addUserByCaptcha");
        ReactiveValueOperations<String, Captcha> captchaOperations = reactiveRedisTemplate.opsForValue();

        return request
                .bodyToMono(User.class)
                .filter(user -> !user.getPhone().equals(""))
                .switchIfEmpty(Mono.error(new CommonException("号码错误")))
                .filterWhen(user ->
                        // 检测是否为通行验证码
                        Mono.just(request.queryParam("code").get().equals("00000"))
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(captchaOperations.get(user.getPhone())
                                        // 核验验证码
                                        .filter(captcha -> request.queryParam("code").get().equals(captcha.getCode()))
                                        .hasElement())
                                .hasElement()
                )
                .switchIfEmpty(Mono.error(new CommonException("验证码错误")))
                .filterWhen(user -> repository.findOneByPhone(user.getPhone()).hasElement().map(res -> !res))
                .switchIfEmpty(Mono.error(new CommonException("手机号重复")))
                .flatMap(this::updateToken)
                .flatMap(repository::save)
                .flatMap(user -> ok().bodyValue("创建成功"));
    }

    /**
     * 获取某一用户信息
     */
    public Mono<ServerResponse> getUser(ServerRequest request) {
        log.info("getUser");
        return repository
                .findById(request.pathVariable("uid"))
                .flatMap(user -> ok().bodyValue(user))
                .switchIfEmpty(notFound().build());
    }

    /**
     * 修改部分用户信息
     */
    public Mono<ServerResponse> updateUser(ServerRequest request) {
        log.info("updateUser");
        return request
                .bodyToMono(User.class)
                .flatMap(repository::save)
                .flatMap(user -> ok().bodyValue(user));
    }

    /**
     * 完全覆盖用户信息，除了token
     */
    public Mono<ServerResponse> coverUser(ServerRequest request) {
        log.info("coverUser");
        return request
                .bodyToMono(User.class)
                .flatMap(user -> repository
                        .findById(user.getUid())
                        .filter(oldUser -> oldUser.getToken().equals(user.getToken()))
                        .flatMap(oldUser -> repository.save(user))
                        .switchIfEmpty(Mono.error(new CommonException("token 与 uid 不符")))
                )
                .flatMap(user -> ok().bodyValue(user));
    }

    /**
     * 通过token认证查询信息
     */
    public Mono<ServerResponse> loginByToken(ServerRequest request) {
        log.info("loginByToken");
        // 获取token
        String tokenStr = request.headers().header("Authorization").get(0);
        // 获取redis操作器
        ReactiveValueOperations<String, Token> operations = reactiveRedisTemplate.opsForValue();

        return operations
                .get(tokenStr)
                .flatMap(token -> repository.findById(token.getUid()))
                .flatMap(user -> ok().bodyValue(user));
    }

    /**
     * 通过密码登录
     */
    public Mono<ServerResponse> loginByPw(ServerRequest request) {
        log.info("loginByPw");
        return request
                .bodyToMono(UserPPUnit.class)
                .flatMap(userUnit -> repository.findOneByPhone(userUnit.getPhone())
                        .switchIfEmpty(Mono.error(new CommonException("手机号无效")))
                        .filter(user // 通行密码 或 正确密码
                                -> userUnit.authPw.equals(userUnit.getPassword())
                                || user.digPassword().equals(userUnit.getPassword()))
                        .switchIfEmpty(Mono.error(new CommonException("密码错误"))))
                // 更新token
                .flatMap(this::updateToken)
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user));
    }

    /**
     * 通过验证码登录
     */
    public Mono<ServerResponse> loginByCaptcha(ServerRequest request) {
        log.info("loginByCaptcha");
        ReactiveValueOperations<String, Captcha> operations = reactiveRedisTemplate.opsForValue();

        return request
                .bodyToMono(UserPCUnit.class)
                .flatMap(userUnit -> operations.get(userUnit.getPhone())
                        .switchIfEmpty(Mono.error(new CommonException("无短信记录或过期")))
                        .filter(captcha // 通行密码 或 正确密码
                                -> userUnit.authCode.equals(userUnit.getCode())
                                || captcha.getCode().equals(userUnit.getCode()))
                        .switchIfEmpty(Mono.error(new CommonException("验证码错误"))))
                .flatMap(captcha -> repository.findOneByPhone(captcha.getPhone()))
                .switchIfEmpty(Mono.error(new CommonException("该用户不存在")))
                // 更新验证码
                .flatMap(this::updateToken)
                .flatMap(user -> ok().bodyValue(user));
    }

    /**
     * 清除token使登录失效
     */
    public Mono<ServerResponse> deleteToken(ServerRequest request) {
        log.info("deleteToken");
        // 获取token
        String token = request.headers().header("Authorization").get(0);
        // 获取redis控制器
        ReactiveValueOperations<String, Token> operations = reactiveRedisTemplate.opsForValue();

        return operations
                .delete(token)
                .filter(Boolean::booleanValue)
                .flatMap(res -> ok().bodyValue("退出登录"))
                .switchIfEmpty(Mono.error(new CommonException("验证都通过了的token呢？ 删除失败？")));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class UserPPUnit {
        final static String authPw = "670b14728ad9902aecba32e22fa4f6bd";
        private String password;
        private String phone;

        @SneakyThrows
        public void setPassword(String pw) {
            password = DigestUtils.md5DigestAsHex(pw.getBytes("UTF-8"));
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class UserPCUnit {
        public String authCode = "00000";
        private String phone;
        private String code;
    }

    /**
     * 更新用户token，删除旧token
     * @return
     */
    @SneakyThrows
    private Mono<User> updateToken(User u) {
        // 获取redis控制器
        ReactiveValueOperations<String, Token> operations = reactiveRedisTemplate.opsForValue();
        // 新旧token
        String oldT = u.getToken();
        String seed = u.toString() + new Date().getTime();
        String token;
        token = DigestUtils.md5DigestAsHex(seed.getBytes(StandardCharsets.UTF_8));

        // 实例对象设置新的
        u.setToken(token);

        // 删除缓存
        if (oldT != null)
            operations.delete(oldT).subscribe(aBoolean -> {
                        if (!aBoolean) log.warn("删除token失败");
                    });

        // 缓存新的
        operations.set(u.getToken(), new Token(u.getUid(), u.getToken()), java.time.Duration.ofDays(5))
                .subscribe(res -> log.info("updateToken " + res + " " + u.getUid() + " " + u.getToken()));

        // 更新表
        return repository.save(u);
    }
}

