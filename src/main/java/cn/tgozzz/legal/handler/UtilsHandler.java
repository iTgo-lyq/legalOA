package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Permission;
import cn.tgozzz.legal.domain.Role;
import cn.tgozzz.legal.repository.RoleRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class UtilsHandler {

    private final RoleRepository repository;

    public UtilsHandler(RoleRepository repository) {
        this.repository = repository;
    }

    /**
     * 根据role合并计算permission
     */
    public Mono<ServerResponse> mergeRoles(ServerRequest request) {
        log.info("mergeRoles");

        return request.bodyToMono(MergeRolesUnit.class)
                .map(MergeRolesUnit::getRoles)
                .flux()
                .flatMap(strings -> Flux.fromStream(strings.stream()))
                .flatMap(repository::findById)
                .map(Role::getPermission)
                .reduce(Permission::merge)
                .switchIfEmpty(Mono.just(new Permission()))
                .flatMap(permission -> ok().contentType(APPLICATION_JSON).bodyValue(permission));
    }

    @Data
    @NoArgsConstructor
    private static class MergeRolesUnit {
        private ArrayList<String> roles;
    }
}
