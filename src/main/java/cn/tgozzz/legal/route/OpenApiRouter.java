package cn.tgozzz.legal.route;

import cn.tgozzz.legal.filter.SmsCaptchaFilter;
import cn.tgozzz.legal.handler.*;
import cn.tgozzz.legal.utils.Checker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Objects;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

@Configuration
public class OpenApiRouter {

    @Bean
    RouterFunction<ServerResponse> officeRouter(OfficeHandler handler) {

        return route()
                .POST("/open-api/office/convert", handler::convert)
                .build();
    }

    RouterFunction<ServerResponse> verifyRouter(VerifyHandler handler) {
        return route()
                .GET("/open-api/verify", handler::verify)
                .build();
    }

    /**
     * 图片上传下载相关接口，不支持覆盖
     */
    @Bean
    RouterFunction<ServerResponse> imageRouter(ImageHandler handler) {

        return nest(path("/open-api/image"),
                // 上传图片
                route(POST("").and(contentType(MULTIPART_FORM_DATA, IMAGE_JPEG, IMAGE_PNG)),
                        handler::uploadImage)
                        //获取图片，可能本地，可能转发至七牛
                .andRoute(GET("/{pid}"), handler::getImage));
    }

    /**
     * 短信验证码相关开放接口
     */
    @Bean
    RouterFunction<ServerResponse> smsRouter(SmsHandler handler) {

        return nest(
                // 短信接口baseUrl
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

    /**
     * word 上传下载格式转换相关接口，不涉及直接操作contract
     */
    @Bean
    RouterFunction<ServerResponse> wordRouter(WordHandler handler) {

        return nest(
                path("/open-api/word"),
                route(GET("/{wid}"), handler::getWord)
                        .andRoute(GET(""), handler::getAllWords)
                        .andRoute(POST("/{wid}").and(contentType(MULTIPART_FORM_DATA)), handler::updateWord)
                        .andRoute(POST("/{wid}").and(contentType(TEXT_PLAIN)), handler::updateHtmlDoc)
                        .andRoute(PUT("/{wid}").and(contentType(MULTIPART_FORM_DATA)), handler::coverWord)
                        .andRoute(PUT("/{wid}").and(contentType(TEXT_PLAIN)), handler::coverHtmlDoc)
                        .andRoute(POST("").and(contentType(MULTIPART_FORM_DATA)), handler::uploadWord)
                        .andRoute(POST("").and(contentType(TEXT_PLAIN)), handler::uploadHtmlDoc)
        );
    }

    /**
     * 自然语言处理相关接口
     */
    @Bean
    RouterFunction<ServerResponse> ltpRouter(LtpHandler handler) {
        // 中文分词(cws)、词性标注(pos)、依存句法分析(dp)、命名实体识别(ner)
        // 语义角色标注(srl)、语义依存 (依存树)(sdp) (依存图)(sdgp)、关键词提取(ke)
        return route().path("/open-api/ltp", builder -> builder
                .POST("/{mode}", handler::LP)
        ).build();
    }
}