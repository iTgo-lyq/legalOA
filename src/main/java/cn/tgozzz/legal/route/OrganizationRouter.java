package cn.tgozzz.legal.route;

import cn.tgozzz.legal.filter.AuthFilter;
import cn.tgozzz.legal.handler.DepartmentHandler;
import cn.tgozzz.legal.handler.RoleHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class OrganizationRouter {

    @Autowired
    private AuthFilter authFilter;

    @Bean
    RouterFunction<ServerResponse> deptRouter(DepartmentHandler handler) {

        return route().path("/organization/departments",
                builder -> builder
                        .GET("", handler::listDepts)
                        .POST("", handler::addDept)
                        .PUT("/{did}", handler::updateDept)
                        .DELETE("/{did}", handler::deleteDept)
                        .build())
                .filter(authFilter::tokenFilter)
                .build();
    }

    @Bean
    RouterFunction<ServerResponse> roleRouter(RoleHandler handler) {

        return route().path("/organization/roles",
                builder -> builder
                        .GET("/permissions", handler::listPermissions)
                        .GET("", handler::listRoles)
                        .POST("", handler::addRole)
                        .PUT("/{rid}", handler::updateRole)
                        .PATCH("/{rid}", handler::changeStatusOfRole)
                        .DELETE("/{rid}", handler::deleteRole)
                        .build())
                .filter(authFilter::tokenFilter)
                .build();
    }
}
