package cn.tgozzz.legal.route;

import cn.tgozzz.legal.filter.AuthFilter;
import cn.tgozzz.legal.handler.UserHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class UsersRouter {

    @Autowired
    private AuthFilter authFilter;

    @Bean
    RouterFunction<ServerResponse> loginRouter(UserHandler handler) {

        return nest(path("/users/login")
                , route
                        (PUT("/pw"), handler::loginByPw).andRoute
                        (PUT("/captcha"), handler::loginByCaptcha))
                .andNest(path("/users/login"), route
                        (GET("/"), handler::loginByToken).andRoute
                        (DELETE("/"), (handler::deleteToken)).
                        filter(authFilter::tokenFilter)
                );
    }

    @Bean
    RouterFunction<ServerResponse> personRouter(UserHandler handler) {
           return route().path("/users",
                   b1 -> b1.nest(accept(APPLICATION_JSON),
                           b2 -> b2
                                   .filter(authFilter::tokenFilter)
                                   .GET("/{uid}", handler::getUser)
                                   .PATCH("/{uid}", handler::updateUser)
                                   .PUT("/{uid}", handler::coverUser)
                                   .build())
                           .POST("/", handler::addUserByCaptcha).build())
                   .build();
    }

    @Bean
    RouterFunction<ServerResponse> userRouter(UserHandler handler) {
        return route()
                .GET("/users", handler::listPeople)
                .POST("/users", handler::createPeople)
                .DELETE("/users", handler::deletePeople)
                .build();
    }
}

//   route().path("/users/login", b1 -> b1
//           .nest(accept(APPLICATION_JSON), b2 -> b2
//           .GET("/{id}", handler::getPerson)
//           .GET("", handler::listPeople)
//           .before(request -> ServerRequest.from(request)
//           .header("X-RequestHeader", "Value")
//           .build()))
//           .DELETE("/users/login/", handler::deleteToken))
//           .build();
