package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Notice;
import cn.tgozzz.legal.domain.Project;
import cn.tgozzz.legal.domain.User;
import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.DepartmentRepository;
import cn.tgozzz.legal.repository.NoticeRepository;
import cn.tgozzz.legal.repository.ProjectRepository;
import cn.tgozzz.legal.repository.UserRepository;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Log4j2
@Component
public class ProjectHandler {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final NoticeRepository noticeRepository;

    public ProjectHandler(ProjectRepository repository, UserRepository userRepository, DepartmentRepository departmentRepository, NoticeRepository noticeRepository) {
        this.projectRepository = repository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.noticeRepository = noticeRepository;
    }

    /**
     * 查询一个项目信息
     */
    public Mono<ServerResponse> getOneProject(ServerRequest request) {
        log.info("getOneProject");
        String pid = request.pathVariable("pid");

        return projectRepository.findById(pid)
                .flatMap(project -> ok().contentType(APPLICATION_JSON).bodyValue(project))
                .switchIfEmpty(Mono.error(new CommonException(404, "pid错误, 或者该项目已被删除")));
    }


    /**
     * 更新一个项目的基本信息
     */
    public Mono<ServerResponse> updateProject(ServerRequest request) {
        log.info("updateProject");
        String pid = request.pathVariable("pid");
        User user = (User) request.attribute("user_info").get();

        return projectRepository.findById(pid)
                .flatMap(project -> request.bodyToMono(Project.BaseInfo.class)
                        .doOnNext(baseInfo -> project.setBaseInfo(baseInfo))
                        .thenReturn(project))
                .doOnNext(project -> project.getHistory().add(new Project.HistoryUnit(user.getName() + " 更新了项目基本信息")))
                .flatMap(projectRepository::save)
                .flatMap(project -> ok().contentType(APPLICATION_JSON).bodyValue(project));
    }

    /**
     * 删除项目
     */
    public Mono<ServerResponse> deleteProject(ServerRequest request) {
        log.info("deleteProject");
        String pid = request.pathVariable("pid");
        User user = (User) request.attribute("user_info").get();

        return projectRepository.findById(pid)
                .switchIfEmpty(Mono.error(new CommonException(404, "pid无效")))
                .doOnNext(project -> project.setStatus(Project.DELETE_STOP))
                .doOnNext(project -> project.getHistory().add(new Project.HistoryUnit(user.getName() + " 删除项目")))
                .flatMap(projectRepository::save)
                .then(ok().bodyValue("删除成功"));
    }

    /**
     * 暂停项目
     */
    public Mono<ServerResponse> stopProject(ServerRequest request) {
        log.info("stopProject");
        String pid = request.pathVariable("pid");
        User user = (User) request.attribute("user_info").get();

        return projectRepository.findById(pid)
                .switchIfEmpty(Mono.error(new CommonException(404, "pid无效")))
                .doOnNext(project -> project.setStatus(Project.STOP_STATUS))
                .doOnNext(project -> project.getHistory().add(new Project.HistoryUnit(user.getName() + " 暂停项目")))
                .flatMap(projectRepository::save)
                .then(ok().bodyValue("暂停成功"));
    }

    /**
     * 重启项目
     */
    public Mono<ServerResponse> restartProject(ServerRequest request) {
        log.info("stopProject");
        String pid = request.pathVariable("pid");
        User user = (User) request.attribute("user_info").get();

        return projectRepository.findById(pid)
                .switchIfEmpty(Mono.error(new CommonException(404, "pid无效")))
                .doOnNext(project -> project.setStatus(Project.RUNNING_STATUS))
                .doOnNext(project -> project.getHistory().add(new Project.HistoryUnit(user.getName() + " 重启项目")))
                .flatMap(projectRepository::save)
                .then(ok().bodyValue("重启成功"));
    }

    /**
     * 添加项目
     * 相关用户绑定项目
     */
    public Mono<ServerResponse> addProject(ServerRequest request) {
        log.info("addProject");
        User user = (User) request.attribute("user_info").get();

        return request.bodyToMono(Project.class)
                .doOnNext(project -> project.setPid(null))
                .doOnNext(project -> project.setCreateInfo(new Project.CreateInfo(user.getUid(), user.getName())))
                .doOnNext(project -> project.getHistory().add(new Project.HistoryUnit(user.getName() + " 创建项目")))
                // 保存项目信息
                .flatMap(projectRepository::save)
                // 创建者绑定项目
                .flatMap(project -> Mono.just(user)
                        // 关联项目
                        .doOnNext(u -> u
                                .getProject()
                                .add(new User.ProjectUnit(project.getPid(), "creator", project.getBaseInfo())))
                        // 添加通知
                        .flatMap(u -> noticeRepository
                                .save(new Notice("system", "html", "您已成功创建项目 <a data-pid='" + project.getPid() + "' class='$class$' href='$link$'>" + project.getBaseInfo().getName() + "</a>"))
                                .doOnNext(notice -> u.getNotice().add(notice.getNid()))
                                .thenReturn(u))
                        .flatMap(userRepository::save)
                        .thenReturn(project))


                // 负责人绑定项目
                .flatMap(project -> Mono.just(project.getDirector().getUid())
                        .flatMap(userRepository::findById)
                        // 关联项目
                        .doOnNext(u -> u
                                .getProject()
                                .add(new User.ProjectUnit(project.getPid(), "director", project.getBaseInfo())))
                        // 添加通知
                        .flatMap(u -> noticeRepository
                                .save(new Notice("system", "html", "您已被指定为项目 <a data-pid='" + project.getPid() + "' class='$class$' href='$link$'>" + project.getBaseInfo().getName() + "</a> 的负责人"))
                                .doOnNext(notice -> u.getNotice().add(notice.getNid()))
                                .thenReturn(u))

                        .flatMap(userRepository::save)
                        .thenReturn(project))


                // 拟稿部门成员绑定项目
                .flatMap(project -> Mono.just(project.getDrafter().getDid())
                        // 获得该部门所有子部门及其自身
                        .flatMap(this::findAllDepartments)
                        .flux()
                        // 查询所有部门有关人员
                        .flatMap(userRepository::findAllInProjects)
                        // 关联项目
                        .doOnNext(u -> u
                                .getProject()
                                .add(new User.ProjectUnit(project.getPid(), "drafter", project.getBaseInfo())))
                        .flatMap(u -> noticeRepository
                                .save(new Notice("system", "html", "项目 <a data-pid='" + project.getPid() + "' class='$class$' href='$link$'>" + project.getBaseInfo().getName() + "</a> 将由您以及您的的团队成员负责 合同拟稿"))
                                .doOnNext(notice -> u.getNotice().add(notice.getNid()))
                                .thenReturn(u))
                        .flatMap(userRepository::save)
                        .collectList()
                        .thenReturn(project))


                // 审核部门成员绑定项目
                .flatMap(project -> Mono.just(project.getAuditor())
                        .flux()
                        .flatMap(auditorUnits -> Flux.fromStream(auditorUnits.stream()))
                        .map(Project.AuditorUnit::getDid)
                        .collectList()
                        // 获得该部门所有子部门及其自身
                        .flatMap(this::findAllDepartments)
                        .flux()
                        // 查询所有部门有关人员
                        .flatMap(userRepository::findAllInProjects)
                        // 关联项目
                        .doOnNext(u -> u
                                .getProject()
                                .add(new User.ProjectUnit(project.getPid(), "auditor", project.getBaseInfo())))
                        .flatMap(u -> noticeRepository
                                .save(new Notice("system", "html", "项目 <a data-pid='" + project.getPid() + "' class='$class$' href='$link$'>" + project.getBaseInfo().getName() + "</a> 将由您以及您的的团队成员负责 合同审核"))
                                .doOnNext(notice -> u.getNotice().add(notice.getNid()))
                                .thenReturn(u))
                        .flatMap(userRepository::save)
                        .collectList()
                        .thenReturn(project))
                .flatMap(project -> ok().contentType(APPLICATION_JSON).bodyValue(project));
    }

    /**
     * 获取该用户关联的所有项目
     */
    public Mono<ServerResponse> listProject(ServerRequest request) {
        log.info("listProject");
        User user = (User) request.attribute("user_info").get();

        ListProjectResult res = new ListProjectResult();

        return Mono.just(user)
                .map(User::getProject)
                .flatMap(list -> Flux.fromStream(list.stream())
                        .filter(unit -> unit.getRole().equals("creator"))
                        .flatMap(projectUnit -> projectRepository.findById(projectUnit.getPid()))
                        .collectList()
                        .doOnNext(projects -> res.getCreator().addAll(projects))
                        .thenReturn(list))
                .flatMap(list -> Flux.fromStream(list.stream())
                        .filter(unit -> unit.getRole().equals("drafter"))
                        .flatMap(projectUnit -> projectRepository.findById(projectUnit.getPid()))
                        .collectList()
                        .doOnNext(projects -> res.getDrafter().addAll(projects))
                        .thenReturn(list))
                .flatMap(list -> Flux.fromStream(list.stream())
                        .filter(unit -> unit.getRole().equals("auditor"))
                        .flatMap(projectUnit -> projectRepository.findById(projectUnit.getPid()))
                        .collectList()
                        .doOnNext(projects -> res.getAuditor().addAll(projects))
                        .thenReturn(list))
                .flatMap(list -> Flux.fromStream(list.stream())
                        .filter(unit -> unit.getRole().equals("director"))
                        .flatMap(projectUnit -> projectRepository.findById(projectUnit.getPid()))
                        .collectList()
                        .doOnNext(projects -> res.getDirector().addAll(projects))
                        .thenReturn(list))
                .thenReturn(user)
                .flatMap(userRepository::save)
                // 返回最终结果
                .thenReturn(res)
                .flatMap(result -> ok().contentType(APPLICATION_JSON).bodyValue(result));
    }

    /**
     * 找出该部门关联的所有子部门以及他本身
     */
    public Mono<ArrayList<String>> findAllDepartments(String did) {
        ArrayList<String> res = new ArrayList<>();
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        AtomicInteger index = new AtomicInteger(-1);

        res.add(did);

        return departmentRepository.findAll()
                .doOnNext(department ->
                        map.put(department.getDid(), department.getSubordinates()))
                .collectList()
                .doOnNext(departments -> {
                    while (index.incrementAndGet() < res.size()) {
                        res.addAll(map.get(res.get(index.get())));
                    }
                })
                .thenReturn(res);
    }

    /**
     * 找出部门关联的所有子部门以及他本身
     */
    private Mono<ArrayList<String>> findAllDepartments(List<String> strings) {
        ArrayList<String> res = new ArrayList<>(strings);

        AtomicInteger index = new AtomicInteger(-1);
        HashMap<String, ArrayList<String>> map = new HashMap<>();

        return departmentRepository.findAll()
                .doOnNext(department ->
                        map.put(department.getDid(), department.getSubordinates()))
                .collectList()
                .doOnNext(departments -> {
                    while (index.incrementAndGet() < res.size()) {
                        res.addAll(map.get(res.get(index.get())));
                    }
                })
                .thenReturn(res);
    }

    @Data
    public static class ListProjectResult {
        private ArrayList<Project> creator = new ArrayList<>();
        private ArrayList<Project> drafter = new ArrayList<>();
        private ArrayList<Project> auditor = new ArrayList<>();
        private ArrayList<Project> director = new ArrayList<>();
    }
}
