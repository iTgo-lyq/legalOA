package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Permission;
import cn.tgozzz.legal.domain.Role;
import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.RoleRepository;
import cn.tgozzz.legal.utils.TokenUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
@Log4j2
public class RoleHandler {

    private final RoleRepository repository;

    private final TokenUtils tokenUtils;

    public RoleHandler(RoleRepository repository, TokenUtils tokenUtils) {
        this.repository = repository;
        this.tokenUtils = tokenUtils;
    }

    /**
     * 查询所有角色
     */
    public Mono<ServerResponse> listRoles(ServerRequest request) {
        log.info("listRoles");

        return repository.findAll()
                .collectList()
                .flatMap(roles -> ok().contentType(APPLICATION_JSON).bodyValue(roles));
    }

    /**
     * 添加角色
     */
    public Mono<ServerResponse> addRole(ServerRequest request) {
        log.info("addRole");

        return request.bodyToMono(Role.class)
                .doOnNext(role -> role.setRid(null))
                .flatMap(repository::save)
                .flatMap(role -> ok().contentType(APPLICATION_JSON).bodyValue(role));
    }

    /**
     * 更新角色
     */
    public Mono<ServerResponse> updateRole(ServerRequest request) {
        log.info("updateRole");
        String rid = request.pathVariable("rid");

        return repository.findById(rid)
                .switchIfEmpty(Mono.error(new CommonException(404, "rid 无效")))
                .flatMap(role -> request
                        .bodyToMono(Role.class)
                        .doOnNext(r -> r.setRid(rid)))
                .flatMap(repository::save)
                .flatMap(role -> ok().contentType(APPLICATION_JSON).bodyValue(role));
    }

    /**
     * 删除角色
     */
    public Mono<ServerResponse> deleteRole(ServerRequest request) {
        log.info("deleteRole");
        String rid = request.pathVariable("rid");

        return repository.findById(rid)
                .switchIfEmpty(Mono.error(new CommonException(404, "rid 无效")))
                .flatMap(repository::delete)
                .then(ok().contentType(TEXT_PLAIN).bodyValue("删除成功"));
    }

    /**
     * 获取空的权限列表
     */
    public Mono<ServerResponse> listPermissions(ServerRequest request) {
        log.info("listPermissions");

        return ok().contentType(APPLICATION_JSON).bodyValue(new Permission());
    }

    /**
     * 切换role的启用状态
     */
    public Mono<ServerResponse> changeStatusOfRole(ServerRequest request) {
        log.info("changeStatusOfRole");
        String rid = request.pathVariable("rid");
        int status;
        try {
            status = Integer.parseInt(request.queryParam("status").get());
        } catch (Exception e) {
            return Mono.error(new CommonException("status 无效"));
        }

        return repository.findById(rid)
                .switchIfEmpty(Mono.error(new CommonException(404, "rid 无效")))
                .doOnNext(role -> role.setStatus(status))
                .flatMap(repository::save).flatMap(role -> ok().bodyValue(role));
    }
}
