package cn.tgozzz.legal.utils;

import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.handler.ContractHandler;
import cn.tgozzz.legal.handler.OfficeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import static org.springframework.http.MediaType.*;

public class Office {

    public static Mono<String> upload(Part file, String tid) {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("uploadedFile", file);

        return WebClient.create("http://legal.tgozzz.cn/office/upload?tid=" + tid)
                .post().contentType(MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(String.class);
    }

    public static Mono<Boolean> copyTemplate(String oldTid, String newTid) {
        return WebClient.create("http://legal.tgozzz.cn/office/copyTemplate?old=" + oldTid + "&new=" + newTid)
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

    public static Mono<String> convert(OfficeHandler.ConvertUnit config) {
        return WebClient.create("http://legal.tgozzz.cn/documentServer/ConvertService.ashx")
                .post()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(config)
                .retrieve()
                .bodyToMono(String.class);
    }

    public static Mono<byte[]> download(String url) {
        return WebClient.create(url)
                .get()
                .accept(APPLICATION_OCTET_STREAM)
                .exchange()
                .flatMap(response ->
                        response.bodyToMono(ByteArrayResource.class))
                .map(ByteArrayResource::getByteArray);
    }

    public static Mono<byte[]> downloadAsDocx(DocxConvertUnit config) {

        return WebClient.create("http://legal.tgozzz.cn/documentServer/ConvertService.ashx")
                .post()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(config)
                .retrieve()
                .bodyToMono(DocxConvertResult.class)
                .map(DocxConvertResult::getFileUrl)
                .map(s -> s.replace("localhost", "47.100.222.159"))
                .flatMap(Office::download);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocxConvertUnit {
        private Boolean async = false;
        private String filetype = "doc";
        private String key;
        private String outputtype = "docx";
        private String title;
        private String url;

        public DocxConvertUnit(String cid, String title, String url) {
            this.setKey(cid);
            this.setTitle(title);
            this.setUrl(url);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocxConvertResult {
        private String endConvert;
        private String fileUrl;
        private String percent;
    }
}
