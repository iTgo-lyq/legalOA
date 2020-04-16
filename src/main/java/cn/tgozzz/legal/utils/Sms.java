package cn.tgozzz.legal.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.util.DigestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Log
public class Sms {
    // 在短信宝注册的用户名
    private static final String userName = "13735866541";
    // 在短信宝注册的密码
    private static final String password = "lyq1999==";
    // 短信包API地址
    private static final String httpUrl = "http://api.smsbao.com/sms";

    // 接收者手机号
    public String phone = "";
    // 发送的内容
    public String content = "";

    // 生成query参数
    private static StringBuffer constructQuery(String phone, String content) {
        StringBuffer httpArg = new StringBuffer("?");
        httpArg.append("u=").append(userName).append("&");
        httpArg.append("p=").append(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8))).append("&");
        httpArg.append("m=").append(phone).append("&");
        httpArg.append("c=").append(content);
        return httpArg;
    }

    /**
     * 设置接收者手机号
     *
     * @param p 手机号
     */
    public Sms setPhone(String p) {
        phone = p;
        return this;
    }

    /**
     * 设置发送的内容
     *
     * @param c 短信内容
     */
    public Sms setContent(String c) {
        content = c;
        return this;
    }

    /**
     * 发送短信
     * @return 短信是否发送成功
     */
    public Mono<Boolean> send() {
        log.info("短信 :: " + phone + " :: " + content);

        StringBuffer query = constructQuery(phone, content);

        WebClient client = WebClient.create(httpUrl);

        return client.get().uri(query.toString())
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(res -> log.info("短信 :: " + res))
                .map("0"::equals);
    }
}
