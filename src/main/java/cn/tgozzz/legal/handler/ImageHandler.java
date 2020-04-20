package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Image;
import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.ImageRepository;
import cn.tgozzz.legal.utils.ImageUploader;
import cn.tgozzz.legal.utils.QiNiuReturnBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
@Log4j2
public class ImageHandler {

    static private String baseUrl = "http://q8tpx3tgs.bkt.clouddn.com/";

    private final ImageRepository repository;

    public ImageHandler(ImageRepository repository) {
        // 图片存储
        this.repository = repository;
    }

    /**
     * 上传图片到七牛
     * 如果上传失败，存储到本地
     */
    public Mono<ServerResponse> uploadImage(ServerRequest request) {

        return request.multipartData()
                // multipartData 一堆flux dataBuffer啥啥的转成 Mono Part value
                .map(MultiValueMap::toSingleValueMap)
                .flatMap(partsMap -> {
                    // 获取文件part
                    FilePart file = (FilePart) partsMap.get("file");
                    Mono<String> group = dataBuffer2String(partsMap, "group");
                    Mono<String> tags = dataBuffer2String(partsMap, "tags");
                    if(Objects.isNull(file)) return Mono.error(new CommonException("文件不能为空"));
                    return ImageUploader.execute(file)
                            .map(Image::new)
                            .flatMap(image -> tags
                                    .flatMap(this::json2StringList)
                                    .doOnNext(image::setTags)
                                    .flatMap(s -> group)
                                    .doOnNext(image::setGroup)
                                    .map(s -> image)
                            )
                            .flatMap(repository::save);
                }).flatMap(image -> ok().bodyValue(image));
    }


    /**
     * 重定向到七牛的地址，或者从本地读取
     */
    public Mono<ServerResponse> getImage(ServerRequest request) {

        return permanentRedirect(URI.create(QiNiuReturnBody.baseUrl)).build();
    }

    /**
     * json 字符串转数组
     */
    @SneakyThrows
    private Mono<List<String>> json2StringList(String s) {
        ObjectMapper mapper = new ObjectMapper();
        if(s.equals("")) return Mono.just(Arrays.asList(new String[0]));
        String[] arr = mapper.readValue(s, String[].class);
        return Mono.just(Arrays.asList(arr));
    }

    /**
     * dataBuffer 转 字符串
     * 煞笔java
     */
    private Mono<String> dataBuffer2String(Map<String, Part> map, String name) {
        Part p = map.get(name);
        if(Objects.isNull(p)) return Mono.just("");
        return p.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .reduce((s, s2) -> s+s2);
    }
}