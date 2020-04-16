package cn.tgozzz.legal.filter;

import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class SmsCaptchaFilter {

    public static Mono<ServerResponse> checkPhone(ServerRequest serverRequest, HandlerFunction<ServerResponse> next) {
        return next.handle(serverRequest);
    }
}
