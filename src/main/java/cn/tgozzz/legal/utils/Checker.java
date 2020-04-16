package cn.tgozzz.legal.utils;

import org.springframework.web.reactive.function.server.ServerRequest;

public class Checker {

    final static String REGEX_MOBILE = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";

    /**
     * 手机号校验
     */
    public static boolean phone(ServerRequest serverRequest) {

        String phone = serverRequest.pathVariable("phone");

        return phone.matches(REGEX_MOBILE);
    }
}
