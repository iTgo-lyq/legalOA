package cn.tgozzz.legal.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Configuration
public class IndexRouter {
    @Bean
    public RouterFunction<ServerResponse> indexRoute(
            @Value("classpath:dist/index.html") final Resource indexHtml) {
        return route(GET(""), request -> ok().contentType(TEXT_HTML).bodyValue(indexHtml));
    }
}
