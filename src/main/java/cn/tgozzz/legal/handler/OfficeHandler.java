package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.utils.Office;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
public class OfficeHandler {

    public Mono<ServerResponse> convert(ServerRequest request) {

        return request.bodyToMono(ConvertUnit.class)
                .flatMap(Office::convert)
                .flatMap(s -> ok().contentType(APPLICATION_JSON).bodyValue(s));
    }

    @Data
    @NoArgsConstructor
    public static class ConvertUnit {
        private String filetype;
        private String key;
        private String outputtype;
        private Thumbnail thumbnail;
        private String title;
        private String url;

        @Data
        @NoArgsConstructor

        public static class Thumbnail {
            private int aspect;
            private boolean first;
            private int height;
            private int width;
        }
    }
}
