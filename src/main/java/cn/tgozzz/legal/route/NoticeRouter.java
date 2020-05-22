package cn.tgozzz.legal.route;

import cn.tgozzz.legal.handler.NoticeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class NoticeRouter {

    @Bean
    RouterFunction<ServerResponse> noticeRoute(NoticeHandler handler) {
        return RouterFunctions.route(GET("/notice/{nid}"), handler::getNotice)
                .andRoute(POST("/notice/list"), handler::listNotice);
    }
}
