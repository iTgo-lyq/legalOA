package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Captcha;
import cn.tgozzz.legal.domain.Template;
import cn.tgozzz.legal.domain.Token;
import cn.tgozzz.legal.domain.User;
import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.TemplateRepository;
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
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Log4j2
@Component
public class UserHandler {

    private final UserRepository repository;

    private final TemplateRepository templateRepository;

    private final ReactiveRedisTemplate reactiveRedisTemplate;

    public UserHandler(UserRepository repository, TemplateRepository templateRepository, ReactiveRedisTemplate reactiveRedisTemplate) {

        this.repository = repository;
        this.templateRepository = templateRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    /**
     * 获取用户列表
     */
    public Mono<ServerResponse> listPeople(ServerRequest request) {
        log.info("listPeople");
        return repository
                .findAll().collectList()
                .flatMap(res -> ok().contentType(APPLICATION_JSON).bodyValue(res));
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
                .flatMap(list -> ok().contentType(APPLICATION_JSON).bodyValue(list));
    }

    /**
     * 删除所有用户
     * 提供用户数组则按数组删除
     */
    public Mono<ServerResponse> deletePeople(ServerRequest request) {
        log.info("deletePeople");

        return request.bodyToMono(DeletePeopleUnit.class)
                .onErrorResume(throwable -> Mono.empty())
                .map(DeletePeopleUnit::getList)
                .flux()
                .flatMap(strings -> Flux.fromStream(strings.stream()))
                .flatMap(s -> repository.deleteById(s))
                .switchIfEmpty(repository.deleteAll())
                .collectList()
                .then(ok().bodyValue("删除完毕"));
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
                                .filter(Boolean::booleanValue)
                                .hasElement()
                )
                .switchIfEmpty(Mono.error(new CommonException("验证码错误")))
                .filterWhen(user -> repository.findOneByPhone(user.getPhone()).hasElement().map(res -> !res))
                .switchIfEmpty(Mono.error(new CommonException("手机号重复")))
                .flatMap(repository::save)
                .flatMap(this::updateToken)
                .flatMap(repository::save)
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user));
    }

    /**
     * 获取某一用户信息
     */
    public Mono<ServerResponse> getUser(ServerRequest request) {
        log.info("getUser");
        return repository
                .findById(request.pathVariable("uid"))
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user))
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
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user));
    }

    /**
     * 完全覆盖用户信息，除了token、密码
     */
    public Mono<ServerResponse> coverUser(ServerRequest request) {
        log.info("coverUser");
        return request
                .bodyToMono(User.class)
                .flatMap(user -> repository
                        .findById(user.getUid())
                        .filter(oldUser -> oldUser.getToken().equals(user.getToken()))
                        .doOnNext(oldUser -> user.embedPassword(oldUser.getPassword()))
                        .flatMap(oldUser -> repository.save(user))
                        .switchIfEmpty(Mono.error(new CommonException("token 与 uid 不符")))
                )
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user));
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

        Mono<User> defaultU = tokenStr.equals("token") ? repository
                .findOneByName("system")
                .switchIfEmpty(Mono.error(new CommonException(403, "管理员尚未建立")))
                : Mono.error(new CommonException(403, "中奖了，token 执行中 过期, 执行到哪一步俺也不知道"));

        return operations
                .get(tokenStr)
                .flatMap(token -> repository.findById(token.getUid()))
                .switchIfEmpty(defaultU)
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user));
    }

    /**
     * 通过密码登录
     */
    public Mono<ServerResponse> loginByPw(ServerRequest request) {
        log.info("loginByPw");
        return request
                .bodyToMono(UserPPUnit.class)
                .flatMap(userUnit -> repository.findOneByPhone(userUnit.getPhone())
                        .switchIfEmpty(Mono.error(new CommonException("找不到手机号")))
                        .filter(user // 通行密码 或 正确密码
                                -> UserPPUnit.authPw.equals(userUnit.getPassword())
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
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user));
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
                .flatMap(res -> ok().contentType(APPLICATION_JSON).bodyValue("退出登录"))
                .switchIfEmpty(Mono.error(new CommonException("验证都通过了的token呢？ 删除失败？")));
    }

    /**
     * 添加模板收藏
     * 模板被收藏数+1
     */
    public Mono<ServerResponse> markTemplate(ServerRequest request) {
        log.info("markTemplate");
        String uid = request.pathVariable("uid");

        return request.bodyToMono(MarkTemplateUnit.class)
                .map(unit -> unit.getTid())
                .flatMap(templateRepository::findById)
                .switchIfEmpty(Mono.error(new CommonException(404, "模板id无效")))
                .doOnNext(Template::addStar)
                .flatMap(template -> repository
                        .findById(uid)
                        .switchIfEmpty(Mono.error(new CommonException(404, "用户id无效")))
                        .filter(user -> user.markTemplate(template.getTid()))
                        .flatMap(user -> templateRepository
                                .save(template)
                                .map(template1 -> user))
                        .switchIfEmpty(Mono.error(new CommonException("重复收藏"))))
                .flatMap(repository::save)
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user.getTemplate()));
    }

    /**
     * 取消模板收藏
     */
    public Mono<ServerResponse> cancelMarkTemplate(ServerRequest request) {
        log.info("cancelMarkTemplate");
        String uid = request.pathVariable("uid");
        String tid = request.pathVariable("tid");

        return repository.findById(uid)
                .switchIfEmpty(Mono.error(new CommonException(404, "用户id无效")))
                .filter(user -> user.cancelMarkTemplate(tid))
                .switchIfEmpty(Mono.error(new CommonException("模板未收藏")))
                .flatMap(repository::save)
                .flatMap(user -> templateRepository.findById(tid))
                .doOnNext(Template::reduceStar)
                .flatMap(templateRepository::save)
                .flatMap(template -> ok().bodyValue("取消收藏"))
                .switchIfEmpty(ok().bodyValue("模板id无效，收藏已取消"));
    }

    /**
     * 切换用户状态
     */
    public Mono<ServerResponse> changeStatus(ServerRequest request) {
        log.info("changeStatus");
        String uid = request.pathVariable("uid");
        int status;
        try {
            status = Integer.parseInt(request.queryParam("status").get());
        } catch (Exception e) {
            return Mono.error(new CommonException("参数错误"));
        }

        return repository.findById(uid)
                .switchIfEmpty(Mono.error(new CommonException(404, "uid 无效")))
                .doOnNext(user -> user.getOrganization().setStatus(status))
                .flatMap(repository::save)
                .flatMap(user -> ok().bodyValue("ok"));
    }

    /**
     * 重置密码
     */
    public Mono<ServerResponse> resetPassword(ServerRequest request) {
        log.info("resetPassword");
        String uid = request.pathVariable("uid");

        return request.bodyToMono(ResetPasswordUnit.class)
                .flatMap(unit -> repository.findById(uid)
                        .switchIfEmpty(Mono.error(new CommonException(404, "uid 无效")))
                        .doOnNext(user -> user.setPassword(unit.getPassword()))
                        .flatMap(repository::save))
                .flatMap(user -> ok().bodyValue("修改成功"));
    }

    /**
     * 删除用户
     */
    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        log.info("deleteUser");
        String uid = request.pathVariable("uid");

        return repository.findById(uid)
                .switchIfEmpty(Mono.error(new CommonException(404, "uid 无效")))
                .flatMap(repository::delete)
                .then(ok().bodyValue("删除成功"));
    }

    /**
     * 链接到头像
     */
    public Mono<ServerResponse> getPortrait(ServerRequest request) {
        String uid = request.pathVariable("uid");

        return repository.findById(uid)
                .map(User::getPortrait)
                .flatMap(s -> ServerResponse.permanentRedirect(URI.create(s)).build())
                .switchIfEmpty(ServerResponse.permanentRedirect(URI.create(User.portraits_sample[0])).build());
    }

    /**
     * 添加签名
     */
    public Mono<ServerResponse> addSign(ServerRequest request) {
        String uid = request.pathVariable("uid");

        return repository.findById(uid)
                .switchIfEmpty(Mono.error(new CommonException(404, "uid无效")))
                .flatMap(user -> request.bodyToMono(String.class)
                        .doOnNext(s -> user.getSigns().add(s))
                        .then(repository.save(user)))
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user));
    }

    /**
     * 删除签名
     */
    public Mono<ServerResponse> deleteSign(ServerRequest request) {
        String uid = request.pathVariable("uid");
        int index = Integer.parseInt(request.pathVariable("index"));

        return repository.findById(uid)
                .switchIfEmpty(Mono.error(new CommonException(404, "uid无效")))
                .doOnNext(user -> user.getSigns().remove(index))
                .flatMap(repository::save)
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user));
    }

    /**
     * 设置默认签名
     */
    public Mono<ServerResponse> setDefaultSign(ServerRequest request) {
        String uid = request.pathVariable("uid");
        int index = Integer.parseInt(request.pathVariable("index"));

        return repository.findById(uid)
                .switchIfEmpty(Mono.error(new CommonException(404, "uid无效")))
                .doOnNext(user -> {
                    ArrayList<String> signs =  user.getSigns();
                    String sign = signs.get(index);
                    signs.remove(index);
                    signs.add(0, sign);
                })
                .flatMap(repository::save)
                .flatMap(user -> ok().contentType(APPLICATION_JSON).bodyValue(user));
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

    @Data
    @NoArgsConstructor
    static class MarkTemplateUnit {
        private String tid;
    }

    @Data
    @NoArgsConstructor
    static class ResetPasswordUnit {
        private String password;
    }

    @Data
    @NoArgsConstructor
    static class DeletePeopleUnit {
        ArrayList<String> list;
    }

    /**
     * 更新用户token，删除旧token
     *
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

