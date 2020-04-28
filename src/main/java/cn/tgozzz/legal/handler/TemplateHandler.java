package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Template;
import cn.tgozzz.legal.domain.TemplateGroup;
import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.TemplateGroupRepository;
import cn.tgozzz.legal.repository.TemplateRepository;
import cn.tgozzz.legal.utils.Office;
import cn.tgozzz.legal.utils.TokenUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
@Slf4j
public class TemplateHandler {

    private final TemplateRepository tempRepository;
    private final TokenUtils tokenUtils;
    private final TemplateGroupRepository tempGroupRepository;

    TemplateHandler(TemplateRepository tempRepository, TokenUtils tokenUtils, TemplateGroupRepository tempGroupRepository) {
        this.tempRepository = tempRepository;
        this.tokenUtils = tokenUtils;
        this.tempGroupRepository = tempGroupRepository;
    }

    /**
     * 获取指定模板信息
     */
    public Mono<ServerResponse> getOneTemp(ServerRequest request) {
        log.info("getOneTemp");
        String tid = request.pathVariable("tid");

        return tempRepository.findById(tid)
                .flatMap(template -> ok().bodyValue(template))
                .switchIfEmpty(Mono.error(new CommonException(404, "id无效")));
    }

    /**
     * 上传模板文件
     * 文件转存到office服务器
     */
    public Mono<ServerResponse> uploadTemp(ServerRequest request) {
        log.info("uploadTemp");
        String tgid = request.pathVariable("tgid");

        return request.multipartData()
                .map(map -> map.get("file"))
                .flux()
                .flatMap(parts -> Flux.fromStream(parts.stream()))// 多文件转流
                .flatMap(part -> {
                    Template template = new Template(); // 创建模板记录
                    template.setName(((FilePart) part).filename());
                    template.setGroup(tgid);
                    return tempRepository.save(template)// 拿到记录实体
                            .flatMap(t -> Office.upload(part, t.getTid()) // 上传office服务器
                                    .map(s -> s.contains("filename"))
                                    .doOnNext(aBoolean -> { // 上传成功，设置uri，否则设null
                                        if (aBoolean)
                                            t.setUri("http://legal.tgozzz.cn/office/files/__ffff_127.0.0.1/" + t.getTid());
                                        else
                                            t.setUri(null);
                                    })
                                    .flatMap(aBoolean -> tempRepository.save(t))
                            );
                })
                .collectList()
                .flatMap(templates -> ok().bodyValue(templates));
    }

    /**
     * 确定添加模板到指定模板组
     * //     * 添加到我的模板
     */
    public Mono<ServerResponse> addTemp(ServerRequest request) {
        log.info("addTemp");
        String tgid = request.pathVariable("tgid");

        return request.bodyToMono(AddTempUnit.class)
                .flatMap(unit -> tempGroupRepository //模板组状态更新
                        .findById(tgid)
                        .filter(templateGroup -> unit.getList().size() != 0)
                        .switchIfEmpty(Mono.error(new CommonException("空模板")))
                        .doOnNext(templateGroup -> templateGroup.addTemplate(unit.getList(), unit.getInfo()))
                        .flatMap(tempGroupRepository::save)
                        .switchIfEmpty(Mono.error(new CommonException(404, "模板组id无效"))))
                .flatMap(templateGroup -> ok().bodyValue(templateGroup));
    }

    /**
     * 根据模板组id获取所有模板
     * 可选择按照分页模式返回部分结果
     */
    public Mono<ServerResponse> listTemp(ServerRequest request) {
        log.info("listTemp");
        String tgid = request.pathVariable("tgid");
        boolean lazy = true;
        Integer num = null;
        Integer page = null;
        try {
            num = Integer.valueOf(request.queryParam("num").get());
            page = Integer.valueOf(request.queryParam("page").get());
        } catch (NumberFormatException | NoSuchElementException e) {
            lazy = false;
        }

        boolean finalLazy = lazy;
        Integer finalNum = num;
        Integer finalPage = page;

        return tempGroupRepository
                .findById(tgid)
                .switchIfEmpty(Mono.error(new CommonException(404, "tgid错误")))
                .map(TemplateGroup::getTemplates)
                .map(strings -> finalLazy ?
                        strings.subList(Math.min(finalNum * (finalPage - 1), strings.size()), Math.min(finalNum * finalPage, strings.size()))
                        : strings)
                .flux()
                .flatMap(strings -> Flux.fromStream(strings.stream()))
                .flatMap(tempRepository::findById)
                .collectList()
                .flatMap(templates -> ok().contentType(APPLICATION_JSON).bodyValue(templates));
    }

    /**
     * 复制模板，生成新tid
     * 选择更新模式：覆盖 or 新增 （必须选）
     * 覆盖：模板组内覆盖tid, baseT追加旧tid
     * 新增：模板组内追加tid, 清空旧模板信息, 添加owner等相关属性, baseT重置到唯一旧tid
     */
    public Mono<ServerResponse> applyToUpdateTemp(ServerRequest request) {
        log.info("applyToUpdateTemp");
        String tid = request.pathVariable("tid");
        String mode = request.queryParam("mode").orElse("extend").trim();

        if (!(mode.equals("cover") | mode.equals("extend"))) return Mono.error(new CommonException(400, "模式无效"));

        return tempRepository.findById(tid)
                .switchIfEmpty(Mono.error(new CommonException(404, "模板id无效")))
                .doOnNext(template -> template.setTid(null))
                .flatMap(tempRepository::save) //获取新的id
                .flatMap(template ->
                     tokenUtils.getUser(request) //获取用户信息
                            .doOnNext(user -> {
                                if (mode.equals("cover")){
                                    template.updateByCover(tid, user);
                                    user.updateMineTemp(tid, template.getTid());
                                } else{
                                    template.updateByExtend(tid, user);
                                    user.updateMineTemp(template.getTid());
                                }
                            })
                            .flatMap(tokenUtils::saveUser)
                            .map(user -> template))
                .flatMap(tempRepository::save)
                .flatMap(template -> ok().bodyValue(template));
    }

    /**
     * 更新模板信息
     * 更新模板组动态（更新了模板 or 创建了模板）
     */
    public Mono<ServerResponse> updateTemp(ServerRequest request) {
        log.info("updateTemp");
        String tgid = request.pathVariable("tgid");
        String tid = request.pathVariable("tid");

        return tempRepository.findById(tid)
                .flatMap(template -> {
                    String mode = template.updateMode();
                    return tempGroupRepository.findById(tgid)
                            .switchIfEmpty(Mono.error(new CommonException(404, "模板组id无效")))
                            .flatMap(group -> {
                                if (mode.equals("cover"))
                                    group.coverTemplate(template);
                                else
                                    group.extendTemplate(template);

                                return tempGroupRepository.save(group);
                            });
                })
                .flatMap(group -> ok().bodyValue(group));
    }

    /**
     * 删除模板
     * 更新模板组动态
     */
    public Mono<ServerResponse> deleteTmp(ServerRequest request) {
        log.info("deleteTmp");
        String tid = request.pathVariable("tgid");
        String tgid = request.pathVariable("tgid");

        return tempGroupRepository
                .findById(tgid)
                .switchIfEmpty(Mono.error(new CommonException(404, "tgid 无效")))
                .doOnNext(templateGroup -> tempRepository
                        .findById(tid)
                        .switchIfEmpty(Mono.error(new CommonException(404, "tgid无效")))
                        .doOnNext(template -> templateGroup.setUpdateInfo("用户" + null + " 删除了 模板" + template.getName()))
                        .flatMap(tempRepository::delete))
                .flatMap(tempGroupRepository::save)
                .flatMap(aVoid -> ok().build());
    }

    /**
     * 添加模板组
     * //     * 设置owner
     */
    public Mono<ServerResponse> addGroup(ServerRequest request) {
        log.info("addGroup");

        return request
                .bodyToMono(UpdateGroupUnit.class)
                .map(groupUnit -> {
                    TemplateGroup group = new TemplateGroup();
                    group.setCategory(groupUnit.getCategory());
                    group.setPermission(groupUnit.getPermission());
                    group.setInfo(groupUnit.getInfo());
                    group.setOwner("system");
                    return group;
                })
                .flatMap(tempGroupRepository::save)
                .flatMap(group -> ok().contentType(APPLICATION_JSON).bodyValue(group))
                .switchIfEmpty(Mono.error(new CommonException(500, "存储失败")));
    }

    /**
     * 返回所有模板组列表
     */
    public Mono<ServerResponse> listGroup(ServerRequest request) {
        log.info("listGroup");
        return tempGroupRepository
                .findAll()
                .collectList()
                .flatMap(templateGroups ->
                        ok().contentType(APPLICATION_JSON).bodyValue(templateGroups));
    }

    /**
     * 修改模板组信息
     */
    public Mono<ServerResponse> updateGroup(ServerRequest request) {
        log.info("updateGroup");
        String tgid = request.pathVariable("tgid");

        return request
                .bodyToMono(UpdateGroupUnit.class)
                .flatMap(groupUnit -> tempGroupRepository
                        .findById(tgid)
                        .switchIfEmpty(Mono.error(new CommonException(404, "tgid 错误， 找不到模板组")))
                        .map(templateGroup -> {
                            templateGroup.setCategory(groupUnit.getCategory());
                            templateGroup.setPermission(groupUnit.getPermission());
                            templateGroup.setInfo(groupUnit.getInfo());
                            return templateGroup;
                        })
                )
                .flatMap(tempGroupRepository::save)
                .flatMap(templateGroup -> ok().contentType(APPLICATION_JSON).bodyValue(templateGroup));
    }

    /**
     * 删除模板组
     */
    public Mono<ServerResponse> deleteGroup(ServerRequest request) {
        log.info("deleteGroup");
        String tgid = request.pathVariable("tgid");

        return tempGroupRepository
                .findById(tgid)
                .switchIfEmpty(Mono.error(new CommonException(404, "tgid 错误， 找不到模板")))
                .flatMap(tempGroupRepository::delete)
                .flatMap(aVoid -> ok().bodyValue("删除成功"));
    }

    @Data
    @NoArgsConstructor
    private static class UpdateGroupUnit {
        private String category;
        private String permission;
        private String info;
    }

    @Data
    @NoArgsConstructor
    private static class AddTempUnit {
        private ArrayList<String> list;
        private String info = "用户XX 新增模板 ";
    }
}
