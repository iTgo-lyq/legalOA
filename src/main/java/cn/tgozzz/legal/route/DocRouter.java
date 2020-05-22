package cn.tgozzz.legal.route;

import cn.tgozzz.legal.handler.DocHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class DocRouter {

    @Bean
    RouterFunction<ServerResponse> docRoute(DocHandler handler) {
        return RouterFunctions.route(RequestPredicates.GET("/doc/api"), handler::toApidoc);
    }
}
