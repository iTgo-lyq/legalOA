package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.NoticeRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Log4j2
@Component
public class NoticeHandler {

    private final NoticeRepository repository;

    public NoticeHandler(NoticeRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> getNotice(ServerRequest request) {
        String nid = request.pathVariable("nid");

        return repository.findById(nid)
                .switchIfEmpty(Mono.error(new CommonException(404, "nid无效")))
                .flatMap(notice -> ok().contentType(APPLICATION_JSON).bodyValue(notice));
    }

    public Mono<ServerResponse> listNotice(ServerRequest request) {

        return request.bodyToMono(ArrayList.class)
                .map(arrayList -> (ArrayList<String>) arrayList)
                .flux()
                .flatMap(strings -> Flux.fromStream(strings.stream()))
                .flatMap(repository::findById)
                .collectList()
                .flatMap(notices -> ok().contentType(APPLICATION_JSON).bodyValue(notices));
    }
}
