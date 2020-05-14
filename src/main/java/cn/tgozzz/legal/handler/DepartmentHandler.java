package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Department;
import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.DepartmentRepository;
import cn.tgozzz.legal.utils.TokenUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
@Log4j2
public class DepartmentHandler {

    private final DepartmentRepository repository;

    private final TokenUtils tokenUtils;

    public DepartmentHandler(DepartmentRepository repository, TokenUtils tokenUtils) {
        this.repository = repository;
        this.tokenUtils = tokenUtils;
    }

    /**
     * 查询所有部门
     */
    public Mono<ServerResponse> listDepts(ServerRequest request) {
        log.info("listDepts");

        return repository.findAll()
                .collectList()
                .flatMap(departments -> ok().contentType(APPLICATION_JSON).bodyValue(departments));
    }

    /**
     * 添加部门
     */
    public Mono<ServerResponse> addDept(ServerRequest request) {
        log.info("addDept");

        return request.bodyToMono(Department.class)
                .doOnNext(department -> department.getUpdateInfo().add("创建部门"))
                .flatMap(repository::save)
                // 上级部门添加
                .flatMap(department -> repository.findById(department.getSuperior())
                        .doOnNext(dept -> dept.getSubordinates().add(department.getDid()))
                        .flatMap(repository::save)
                        .thenReturn(department))
                .flatMap(department -> ok().contentType(APPLICATION_JSON).bodyValue(department));
    }

    /**
     * 更新部门信息
     */
    public Mono<ServerResponse> updateDept(ServerRequest request) {
        log.info("updateDept");
        String did = request.pathVariable("did");

        return repository.findById(did)
                .switchIfEmpty(Mono.error(new CommonException(404, "did无效")))
                .flatMap(department -> request
                        .bodyToMono(Department.class)
                        .doOnNext(dept -> dept.setDid(department.getDid())))
                .flatMap(repository::save)
                .flatMap(department -> ok().contentType(APPLICATION_JSON).bodyValue(department));
    }

    /**
     * 删除部门
     */
    public Mono<ServerResponse> deleteDept(ServerRequest request) {
        log.info("deleteDept");
        String did = request.pathVariable("did");

        return repository.findById(did)
                .switchIfEmpty(Mono.error(new CommonException(404, "did 无效")))
                // 上级部门删除
                .flatMap(department -> repository.findById(department.getSuperior())
                        .doOnNext(dept -> dept.getSubordinates().remove(department.getDid()))
                        .flatMap(repository::save)
                        .thenReturn(department))
                .flatMap(repository::delete)
                .then(ok().contentType(TEXT_PLAIN).bodyValue("删除成功"));
    }
}
