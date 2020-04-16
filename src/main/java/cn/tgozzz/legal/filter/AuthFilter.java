package cn.tgozzz.legal.filter;

import cn.tgozzz.legal.domain.Token;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
public class AuthFilter {

    private final ReactiveRedisTemplate reactiveRedisTemplate;

    public AuthFilter(ReactiveRedisTemplate reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    // 校验header的Authorization字段
    public Mono<ServerResponse> tokenFilter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        // 获取uid
        String tokenStr = request.headers().header("Authorization").get(0);
        // redis控制器
        ReactiveValueOperations<String, Token> operations = reactiveRedisTemplate.opsForValue();

        return operations.get(tokenStr)
                .map(Token::getUid)
                .flatMap(token -> next.handle(request))
                .switchIfEmpty(status(HttpStatus.FORBIDDEN).build());
    }
}
