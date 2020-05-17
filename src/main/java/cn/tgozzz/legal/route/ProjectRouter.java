package cn.tgozzz.legal.route;

import cn.tgozzz.legal.filter.AuthFilter;
import cn.tgozzz.legal.handler.ContractHandler;
import cn.tgozzz.legal.handler.ProjectHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class ProjectRouter {

    @Autowired
    private AuthFilter authFilter;

    @Bean
    RouterFunction<ServerResponse> contractRoute(ContractHandler handler) {

        return nest(path("/project/{pid}/contracts"), route()
                .GET("", handler::listContract)
                .POST("", contentType(MULTIPART_FORM_DATA), handler::uploadContract)
                .POST("", contentType(APPLICATION_JSON), handler::createContract)
                .PATCH("", handler::confirmAddContract)
                .PUT("", handler::rollbackContract)
                .build()
                .andNest(path("/{cid}"), route()
                        .GET("", handler::getContract)
                        .PUT("", handler::applyToEdit)
                        .PATCH("", handler::addEditInfo)
                        .DELETE("", handler::deleteContract)
                        .GET("/audit", handler::applyToAudit)
                        .POST("/audit", handler::addAuditInfo)
                        .POST("/back", handler::moveToEdit)
                        .POST("/next", handler::moveToNext)
                        .build()))
                .filter(authFilter::attributeTokenFilter);
    }

    @Bean
    RouterFunction<ServerResponse> projectStatusRoute(ProjectHandler handler) {

        return nest(path("/project/{pid}/status/"), route()
                .PUT("stop", handler::stopProject)
                .PUT("restart", handler::restartProject)
                .build())
                .filter(authFilter::attributeTokenFilter);
    }

    @Bean
    RouterFunction<ServerResponse> projectRoute(ProjectHandler handler) {

        return nest(path("/project"), route()
                .GET("/{pid}", handler::getOneProject)
                .PUT("/{pid}", handler::updateProject)
                .DELETE("/{pid}", handler::deleteProject)
                .POST("", handler::addProject)
                .GET("", handler::listProject)
                .build())
                .filter(authFilter::attributeTokenFilter);
    }
}
