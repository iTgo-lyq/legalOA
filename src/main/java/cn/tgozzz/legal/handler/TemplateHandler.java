package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Template;
import cn.tgozzz.legal.domain.TemplateGroup;
import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.TemplateGroupRepository;
import cn.tgozzz.legal.repository.TemplateRepository;
import cn.tgozzz.legal.utils.Office;
import cn.tgozzz.legal.utils.TokenUtils;
import lombok.AllArgsConstructor;
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
import java.util.stream.Collectors;

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

        return tokenUtils.getUser(request)
                .flatMap(user ->
                        request.multipartData()
                                // 获取文件部分信息
                                .map(map -> map.get("file"))
                                .flux()
                                // 多文件转流
                                .flatMap(parts -> Flux.fromStream(parts.stream()))
                                .flatMap(part -> {
                                    // 创建模板记录
                                    Template template = new Template();
                                    template.setName(((FilePart) part).filename());
                                    template.setGroup(tgid);
                                    template.setOwner(user.getName());
                                    return tempRepository.save(template)
                                            // 拿到模板记录实体
                                            .flatMap(t ->
                                                    // 上传office服务器
                                                    Office.upload(part, t.getTid())
                                                            // 判断上传情况
                                                            .filter(s -> s.contains("filename"))
                                                            .doOnNext(s ->
                                                                    t.setUri("http://legal.tgozzz.cn/office/files/__ffff_127.0.0.1/" + t.getTid())
                                                            )
                                                            .flatMap(aBoolean -> tempRepository.save(t))
                                                            .switchIfEmpty(Mono.error(new CommonException(501, "中奖了，文件上传失败")))
                                            );
                                })
                                .collectList())
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
                // 更新模板信息
                .flatMap(unit -> Mono.just(unit.getList())
                        .flux()
                        .flatMap(strings -> Flux.fromStream(strings.stream()))
                        .flatMap(s -> tempRepository.findByIdAndUpdate(s, tgid))
                        .collectList()
                        .map(temps -> unit)
                )
                //获取更新信息
                .flatMap(unit -> tokenUtils.getUser(request)
                        // 我的模板添加
                        .doOnNext(user -> user.updateMineTemp(AddTempUnit.getTidList(unit)))
                        .flatMap(user -> tempGroupRepository
                                .findById(tgid)
                                .filter(templateGroup -> unit.getList().size() != 0)
                                .switchIfEmpty(Mono.error(new CommonException("空模板")))
                                // 模板组添加信息
                                .doOnNext(templateGroup ->
                                        templateGroup.addTemplate(AddTempUnit.getTidList(unit), unit.getInfo(), user))
                                .flatMap(tempGroupRepository::save)
                                .flatMap(group -> tokenUtils.saveUser(user)
                                        .map(u -> group))
                                .switchIfEmpty(Mono.error(new CommonException(404, "模板组id无效"))))
                )
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
     * 用户新增我的模板
     * office服务器copy对应模板, 返回配置示例
     */
    public Mono<ServerResponse> applyToUpdateTemp(ServerRequest request) {
        log.info("applyToUpdateTemp");
        String oldTid = request.pathVariable("tid");
        String mode = request.queryParam("mode").orElse("extend").trim();

        if (!(mode.equals("cover") | mode.equals("extend"))) return Mono.error(new CommonException(400, "模式无效"));

        return tempRepository.findById(oldTid)
                .switchIfEmpty(Mono.error(new CommonException(404, "模板id无效")))
                .doOnNext(template -> template.setTid(null))
                //获取新的id
                .flatMap(tempRepository::save)
                .flatMap(template -> Office
                        // office服务器copy操作
                        .copyTemplate(oldTid, template.getTid())
                        .filter(Boolean::booleanValue)
                        .flatMap(s -> tokenUtils.getUser(request)
                                //获取用户信息
                                .doOnNext(user -> {
                                    if (mode.equals("cover")) {
                                        template.updateByCover(oldTid, user);
                                        user.updateMineTemp(oldTid, template.getTid());
                                    } else {
                                        template.updateByExtend(oldTid, user, template);
                                        user.updateMineTemp(template.getTid());
                                    }
                                })
                                .flatMap(tokenUtils::saveUser)
                                .map(user -> template)))
                .flatMap(tempRepository::save)
                .flatMap(template -> Office
                        .applyToEdit(template.getTid(), template.getName() + "." + template.getType())
                        .flatMap(s -> Mono.just(new ApplyToUpdateTempSeed(template, s))))
                .flatMap(res -> ok().bodyValue(res));
    }

    /**
     * 更新模板信息
     * 更新模板组动态（更新了模板 or 创建了模板 or 修改了模板信息）
     * 经过此接口后，更新者必定不为空
     */
    public Mono<ServerResponse> updateTemp(ServerRequest request) {
        log.info("updateTemp");
        String tgid = request.pathVariable("tgid");
        String tid = request.pathVariable("tid");

        return tempRepository.findById(tid)
                .switchIfEmpty(Mono.error(new CommonException(404, "模板id无效")))
                // 获取旧模板信息
                .flatMap(template -> {
                    String mode = template.updateMode();
                    // 获取用户信息
                    return tokenUtils.getUser(request)
                            .flatMap(user ->
                                    // 获取新模板更新信息
                                    request.bodyToMono(UpdateTempUnit.class)
                                            // 更新模板文件信息
                                            .map(unit -> {
                                                template.setModifier(user.getUid());
                                                template.setName(unit.getName());
                                                template.setGroup(unit.getGroup());
                                                template.setInfo(unit.getInfo());
                                                template.setUpdateInfo(unit.getUpdateInfo());
                                                return template;
                                            })
                                            .flatMap(tempRepository::save)
                                            // 更新模板组信息
                                            .flatMap(t -> tempGroupRepository
                                                    .findById(t.getGroup())
                                                    .flatMap(group -> {
                                                        // 如果两个组不一致，则删除原来的组中模板
                                                        if (!(tgid.equals(t.getGroup())))
                                                            return tempGroupRepository
                                                                    .findById(template.getGroup())
                                                                    .switchIfEmpty(Mono.error(new CommonException(404, "原模板组id无效")))
                                                                    .doOnNext(g -> g.deleteTemplate(template, g, user))
                                                                    .flatMap(tempGroupRepository::save)
                                                                    .map(g -> group);
                                                        else
                                                            return Mono.just(group);
                                                    }))
                                            .switchIfEmpty(Mono.error(new CommonException(404, "当前模板组id无效")))
                                            .flatMap(group -> {
                                                        if (mode.equals("cover"))
                                                            group.coverTemplate(template, user);
                                                        else
                                                            group.extendTemplate(template, user);
                                                        return Mono.just(group);
                                                    }
                                            ).flatMap(tempGroupRepository::save));
                })
                .flatMap(group -> ok().bodyValue(group));
    }

    /**
     * 删除模板
     * 更新模板组动态
     * 若模板不属于该用户，则模板实际不会被删除
     */
    public Mono<ServerResponse> deleteTmp(ServerRequest request) {
        log.info("deleteTmp");
        String tid = request.pathVariable("tid");
        String tgid = request.pathVariable("tgid");

        return tempGroupRepository
                .findById(tgid)
                .switchIfEmpty(Mono.error(new CommonException(404, "tgid 无效")))
                .flatMap(templateGroup -> tokenUtils
                        .getUser(request)
                        .flatMap(user -> tempRepository
                                .findById(tid)
                                .switchIfEmpty(Mono.error(new CommonException(404, "tid无效")))
                                .doOnNext(template -> templateGroup.deleteTemplate(template, user))
                                // 如果该模板属于该用户，则会被实际删除
                                .filter(user::deleteTemplate)
                                .flatMap(tempRepository::delete)
                                .then(Mono.just(user))
                                .flatMap(tokenUtils::saveUser)
                                .map(u -> templateGroup)
                                .switchIfEmpty(Mono.just(templateGroup))
                        )
                )
                .flatMap(tempGroupRepository::save)
                .flatMap(templateGroup -> ok().contentType(APPLICATION_JSON).bodyValue(templateGroup));
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
                .flatMap(group -> tokenUtils.getUser(request)
                        .doOnNext(user -> group.setOwner(user.getName()))
                        .map(user -> group)
                )
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
                .sort((group1, group2) -> (int) (group2.getCreateTime() - group1.getCreateTime()))
                .doOnNext(templateGroup -> templateGroup.setTemplates(null))
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
                .flatMap(unit -> tokenUtils
                        .getUser(request)
                        .flatMap(user -> tempGroupRepository
                                .findById(tgid)
                                .switchIfEmpty(Mono.error(new CommonException(404, "tgid 错误， 找不到模板组")))
                                .map(templateGroup -> {
                                    templateGroup.setCategory(unit.getCategory());
                                    templateGroup.setPermission(unit.getPermission());
                                    templateGroup.setInfo(unit.getInfo());
                                    templateGroup.setUpdateInfo("用户" + user.getName() + "更新了 模板组信息");
                                    return templateGroup;
                                })))
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
                .then(ok().bodyValue("删除成功"));
    }

    /**
     * 按名称搜索模板
     */
    public Mono<ServerResponse> searchTemplates(ServerRequest request) {
        log.info("searchTemplates");
        String name = request.queryParam("name").get();

        return tempRepository.findAllLikeName(name)
                .collectList()
                .map(templates -> templates.subList(0, Math.min(50, templates.size())))
                .flatMap(templates -> ok().bodyValue(templates));
    }

    /**
     * 根据id获取模板数组
     */
    public Mono<ServerResponse> listTempsById(ServerRequest request) {
        log.info("listTempsById");

        return request.bodyToMono(ListTempsByIdUnit.class)
                .map(ListTempsByIdUnit::getList)
                .flux()
                .flatMap(strings -> Flux.fromStream(strings.stream()))
                .flatMap(tempRepository::findById)
                .collectList()
                .flatMap(templates -> ok().contentType(APPLICATION_JSON).bodyValue(templates));
    }

    @Data
    @NoArgsConstructor
    private static class UpdateTempUnit {
        private String name;
        private String group;
        private String info;
        private String updateInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ApplyToUpdateTempSeed {
        private Template template;
        private String config;
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
    public static class AddTempUnit {
        private ArrayList<subUnit> list;
        private String info = "新增模板 ";

        private static ArrayList<String> getTidList(AddTempUnit u) {
            ArrayList<String> res = new ArrayList<>();
            for (subUnit subU : u.getList())
                res.add(subU.getTid());
            return res;
        }

        @Data
        @NoArgsConstructor
        public static class subUnit {
            private String tid;
            private String info;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ListTempsByIdUnit {
        private ArrayList<String> list;
    }
}
