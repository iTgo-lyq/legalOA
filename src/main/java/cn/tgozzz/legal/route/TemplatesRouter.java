package cn.tgozzz.legal.route;

import cn.tgozzz.legal.filter.AuthFilter;
import cn.tgozzz.legal.filter.TemplateFilter;
import cn.tgozzz.legal.handler.TemplateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class TemplatesRouter {

    @Autowired
    private AuthFilter authFilter;

    @Autowired
    private TemplateFilter templateFilter;

    @Bean
    RouterFunction<ServerResponse> templateItemRoute(TemplateHandler handler) {
        return nest(path("/templates/{tgid}/{tid}"), route()
                .GET("", handler::getOneTemp)
                .PATCH("", handler::updateTemp)
                .PUT("", handler::applyToUpdateTemp)
                .DELETE("", handler::deleteTmp)
                .build())
                .filter(authFilter::tokenFilter);
    }

    @Bean
    RouterFunction<ServerResponse> templateGroupRoute(TemplateHandler handler) {
        return nest(path("/templates/{tgid}"), route()
                .GET("", handler::listTemp)
                .POST("", contentType(MULTIPART_FORM_DATA) ,handler::uploadTemp)
                .POST("", contentType(APPLICATION_JSON) ,handler::addTemp)
                .PUT("", handler::updateGroup)
                .DELETE("", handler::deleteGroup)
                .build())
                .filter(authFilter::tokenFilter);
    }

    @Bean
    RouterFunction<ServerResponse> templateRoute(TemplateHandler handler) {
        return nest(path("/templates"), route()
                .GET("", handler::listGroup)
                .POST("", handler::addGroup)
                .build())
                .filter(authFilter::tokenFilter);
    }
}
