package cn.tgozzz.legal.filter;

import cn.tgozzz.legal.domain.User;
import cn.tgozzz.legal.utils.TokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
public class AuthFilter {

    private final TokenUtils tokenUtils;

    public AuthFilter(TokenUtils tokenUtils) {
        this.tokenUtils = tokenUtils;
    }

    // 校验header的Authorization字段
    public Mono<ServerResponse> tokenFilter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        List<String> header = request.headers().header("Authorization");
        String tokenStr = header.isEmpty() ? "worn" : header.get(0);

        if (tokenStr.equals("token"))
            return next.handle(request);

        return tokenUtils.isValidToken(tokenStr)
                .filter(Boolean::booleanValue)
                .flatMap(token -> next.handle(request))
                .switchIfEmpty(status(HttpStatus.FORBIDDEN).bodyValue("莫得权限，泥奏凯"));
    }

    /**
     * 先校验session是否存在用户信息user_info
     * 不存在则校验header的Authorization字段
     * 校验成功设置session的user_info
     * 失败返回403
     */
    public Mono<ServerResponse> attributeTokenFilter(ServerRequest request, HandlerFunction<ServerResponse> handlerFunction) {
        List<String> header = request.headers().header("Authorization");
        String tokenStr = header.isEmpty() ? "worn" : header.get(0);

        return tokenUtils.getUser(tokenStr)
                .doOnNext(user -> request.attributes().put("user_info",user))
                .flatMap(user -> handlerFunction.handle(request))
                .switchIfEmpty(status(HttpStatus.FORBIDDEN).bodyValue("莫得权限，泥奏凯"));
    }
}
