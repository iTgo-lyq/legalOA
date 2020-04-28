package cn.tgozzz.legal.utils;

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
}
