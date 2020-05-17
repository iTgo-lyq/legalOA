package cn.tgozzz.legal.utils;

import cn.tgozzz.legal.exception.CommonException;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

public class Office {

    public static Mono<String> upload(Part file, String tid) {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("uploadedFile", file);

        return  WebClient.create("http://legal.tgozzz.cn/office/upload?tid=" + tid)
                .post().contentType(MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(String.class);
    }

    public static Mono<Boolean> copyTemplate(String oldTid, String newTid) {
        return  WebClient.create("http://legal.tgozzz.cn/office/copyTemplate?old=" + oldTid + "&new=" + newTid)
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(s -> s.equals("200") ? Mono.just(true) : Mono.error(new CommonException(500, "文件copy失败" + s)));
    }

    public static Mono<String> applyToEdit(String tid, String fileName) {
        return WebClient.create("http://legal.tgozzz.cn/office/applyToEdit?tid=" + tid + "&fileName=" + fileName)
                .get()
                .retrieve()
                .bodyToMono(String.class);
    }

    public static Mono<String> applyToEdit(String tid, String fileName, String mode, String uid, String userName) {
        return WebClient.create("http://legal.tgozzz.cn/office/applyToEdit?" +
                "tid=" + tid +
                "&mode=" + mode +
                "&fileName=" + fileName +
                "&userid=" + uid +
                "&name=" + userName)
                .get()
                .retrieve()
                .bodyToMono(String.class);
    }
}
