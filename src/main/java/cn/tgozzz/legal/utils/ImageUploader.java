package cn.tgozzz.legal.utils;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Log4j2
public class ImageUploader {

    static String accessKey = "lnyYM16IlLldHB3mxUGoq1ukW9NhoZum-MAEpY_h"; // ak
    static String secretKey = "C9zFNzAEjTILt0jnGg2co2cvrsn0UkX6eZlXDmMK"; // sk
    static String bucket = "legal-oa"; // 存储桶
    static long expireSeconds = 3600; // token过期时间
    static String returnBody = QiNiuReturnBody.templete;  // 上传后返回内容模板

    /**
     * 尝试上传到七牛云
     * @param data 客户端 body 的文件内容部分
     * @return
     */
    public static Mono<QiNiuReturnBody> execute(Part data) {

        String token = createAuth();

        MultipartBodyBuilder builder = createBodyBuilder(token, data);

        return  WebClient.create("http://up.qiniup.com/")
                .post().contentType(MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(QiNiuReturnBody.class);
    }

    /**
     * 生成Token
     */
    private static String createAuth() {

        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", returnBody);

        return  Auth.create(accessKey, secretKey).uploadToken(bucket, null, expireSeconds, putPolicy);
    }

    /**
     * 生成请求体
     */
    private static MultipartBodyBuilder createBodyBuilder(String token, Part data) {
        //    resource_key	否	资源名，必须是UTF-8编码。如果上传凭证中 scope 指定为 <bucket>:<key>， 则该字段也必须指定，并且与上传凭证中的 key 一致，否则会报403错误。如果表单没有指定 key，可以使用上传策略saveKey字段所指定魔法变量生成 Key，如果没有模板，则使用 Hash 值作为 Key。
        //    custom_name	否	自定义变量的名字，不限个数。
        //    custom_value	否	自定义变量的值。
        //    upload_token	是	上传凭证，位于 token 消息中。
        //    crc32	否	上传内容的 CRC32 校验码。如果指定此值，则七牛服务器会使用此值进行内容检验。
        //    accept	否	当 HTTP 请求指定 accept 头部时，七牛会返回 Content-Type 头部值。该值用于兼容低版本 IE 浏览器行为。低版本 IE 浏览器在表单上传时，返回 application/json 表示下载，返回 text/plain 才会显示返回内容。
        //    fileName	是	原文件名。对于没有文件名的情况，建议填入随机生成的纯文本字符串。本参数的值将作为魔法变量$(fname)的值使用。
        //    fileBinaryData	是	上传文件的完整内容。

        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("token", token);
        builder.part("file", data);

        return builder;
    }
}