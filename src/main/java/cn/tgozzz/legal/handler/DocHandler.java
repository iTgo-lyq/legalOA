package cn.tgozzz.legal.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class DocHandler {

    public Mono<ServerResponse> toApidoc(ServerRequest request) {
        String TargetUrl = "https://www.eolinker.com/#/share/index?shareCode=61AU5M";
        return ServerResponse.permanentRedirect(URI.create(TargetUrl)).build();
    }
}
