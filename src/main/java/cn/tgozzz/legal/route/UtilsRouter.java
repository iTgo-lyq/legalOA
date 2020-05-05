package cn.tgozzz.legal.route;

import cn.tgozzz.legal.handler.UtilsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class UtilsRouter {

    @Bean
    RouterFunction<ServerResponse> permissionUtilRouter(UtilsHandler handler) {
        return route()
                .POST("/organization/utils/permission/merge", handler::mergeRoles)
                .build();
    }
}
