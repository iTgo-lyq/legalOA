package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.exception.CommonException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Log4j2
@Component
public class VerifyHandler {
    public Mono<ServerResponse> verify(ServerRequest request) {
        String url = request.queryParam("url").orElseThrow();
        String name = request.queryParam("name").orElseThrow();
        String type = request.queryParam("type").orElseThrow();

        return Mono.just(new VerifyResult())
                .flatMap(verifyResult -> ok().contentType(APPLICATION_JSON).bodyValue(verifyResult));
    }

    public static class VerifyResult {
//        boolean
    }
}
