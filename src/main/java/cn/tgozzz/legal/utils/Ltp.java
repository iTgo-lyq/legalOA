package cn.tgozzz.legal.utils;

import cn.tgozzz.legal.exception.CommonException;
import com.baidu.aip.nlp.AipNlp;
import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.nlp.v20190408.NlpClient;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.springframework.web.reactive.function.BodyInserters.*;

/**
 * 自然语言处理 各平台接口整合
 */
@Log4j2
@Component
public class Ltp {
    /******** 讯飞 ** 配置 *******************/
    private static final String WEBTTS_URL_XF = "http://ltpapi.xfyun.cn/v1";
    private static final String APPID_XF = "5e916de9";
    private static final String API_KEY_XF = "7af6d3ba942fc7a72cee5c04fae2be00";
    private static final String TYPE_XF = "dependent";

    /******** 百度 ** 配置 *******************/
    private static final String APP_ID_BD = "19548458";
    private static final String API_KEY_BD = "tfAqyaCu9OZxng2pO4bwIPgW";
    private static final String SECRET_KEY_BD = "RfddKDBNgdLIdXjxvzPfYwEFewxWYs49";

    /******** 腾讯 ** 配置 *******************/
    private static final String API_KEY_TX = "AKIDhCRQ8ZRfKirjsmjpwoRpxtdkbCXbtx87";
    private static final String SECRET_KEY_TX = "91bz6yZtKVQ4bqoVv5z8qHRLxIBpCI7X";

    /**
     * 讯飞接口
     */
    public static Mono<String> analyzeByXF(String mode, String text) {

        WebClient client;
        client = buildXunFeiWebClient();

        return client
                .post().uri("/{mode}", mode)
                .body(fromFormData("text", text))
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * 百度接NLP接口汇总
     */
    public static Mono<String> analyzeByBD(String mode, String... text) {
        AipNlp client = new AipNlp(APP_ID_BD, API_KEY_BD, SECRET_KEY_BD);
        String res;
        try {
        Class ClientClazz = Class.forName("com.baidu.aip.nlp.AipNlp");
        if (mode.equals("wordSimEmbedding") | mode.equals("simnet") | mode.equals("keyword") | mode.equals("topic")) {
            Method duetModeMethod = ClientClazz.getMethod(mode, String.class, String.class, HashMap.class);
            res = duetModeMethod.invoke(client, text[0], text[1], null).toString();
        } else {
            Method soloModeMethod = ClientClazz.getMethod(mode, String.class, HashMap.class);
            res = soloModeMethod.invoke(client, text[0], null).toString();
        }
        } catch (ClassNotFoundException | NoSuchMethodException |IllegalAccessException e) {
            return Mono.error(new CommonException(500, e.getClass() + ": " + e.getMessage()));
        } catch (InvocationTargetException e){
            return Mono.error(new CommonException(500, e.getTargetException().getMessage()));
        }

        return Mono.just(res);
    }

    /**
     * 腾讯NLP接口汇总
     */
    public static Mono<String> analyzeByTX(String mode, String... text) {
        String res;

        // 各项配置
        Credential cred = new Credential(API_KEY_TX, SECRET_KEY_TX);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("nlp.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        NlpClient client = new NlpClient(cred, "ap-guangzhou", clientProfile);

        // 参数设置
        String params;
        if (mode.equals("WordSimilarity") | mode.equals("SentenceSimilarity"))
            params = "{\"SrcWord\":\"" + text[0] + "\",\"TargetWord\":\"" + text[1] + "\"}";
        else
            params = "{\"Text\":\"" + text[0] + "\"}";

        try {
            // 反射到腾讯sdk各个接口
            Class NlpClientClazz = Class.forName("com.tencentcloudapi.nlp.v20190408.NlpClient");
            Class RequestClazz = Class.forName("com.tencentcloudapi.nlp.v20190408.models." + mode + "Request");

            Method modeMethod = NlpClientClazz.getMethod(mode, RequestClazz);
            Method fromJsonString = RequestClazz.getMethod("fromJsonString", String.class, Class.class);
            Method toJsonString = RequestClazz.getMethod("toJsonString", AbstractModel.class);

            res = (String) toJsonString.invoke(null, modeMethod.invoke(client, fromJsonString.invoke(null, params, RequestClazz)));

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e ) {
            return Mono.error(new CommonException(500, e.getClass() + ": " + e.getMessage()));
        } catch (InvocationTargetException e){
            return Mono.error(new CommonException(500, e.getTargetException().getMessage()));
        }

        return Mono.just(res);
    }

    /**
     * 组装讯飞开放api的头部
     */
    private static WebClient buildXunFeiWebClient() {
        String curTime = System.currentTimeMillis() / 1000L + "";
        String param = "{\"type\":\"" + TYPE_XF + "\"}";
        String paramBase64 = new String(Base64.encodeBase64(param.getBytes(StandardCharsets.UTF_8)));
        String checkSum = DigestUtils.md5Hex(API_KEY_XF + curTime + paramBase64);

        return WebClient.builder()
                .uriBuilderFactory(new DefaultUriBuilderFactory(WEBTTS_URL_XF))
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .defaultHeader("X-Param", paramBase64)
                .defaultHeader("X-CurTime", curTime)
                .defaultHeader("X-CheckSum", checkSum)
                .defaultHeader("X-Appid", APPID_XF)
                .build();
    }
}

