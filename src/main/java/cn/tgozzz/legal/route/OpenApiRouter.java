package cn.tgozzz.legal.route;

import cn.tgozzz.legal.filter.SmsCaptchaFilter;
import cn.tgozzz.legal.handler.SmsHandler;
import cn.tgozzz.legal.utils.Checker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Objects;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class OpenApiRouter {

    @Bean
    RouterFunction<ServerResponse> smsRouter(SmsHandler handler) {
        return nest(
                // 短信接口
                path("/open-api/sms"),
                // 验证码相关
                route(GET("/{phone}/captcha")
                        // 无需核对，直接通过
                        .and(queryParam("code", "00000")), handler::noCheckCode)
                        // 核对验证码
                         .andRoute(GET("/{phone}/captcha").and(queryParam("code", Objects::nonNull)), handler::checkCode)
                        // 获取验证码
                        .andRoute(GET("/{phone}/captcha"), handler::getCode)
                        // 发送验证码
                        .andRoute(PUT("/{phone}/captcha").and(Checker::phone), handler::setCode)
                .filter(SmsCaptchaFilter::checkPhone)
        );
    }
}