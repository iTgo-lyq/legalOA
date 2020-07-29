package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.*;
import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.*;
import cn.tgozzz.legal.utils.ImageUtils;
import cn.tgozzz.legal.utils.Office;
import cn.tgozzz.legal.utils.SecurityUtils;
import cn.tgozzz.legal.utils.WordUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Component
@Log4j2
public class ContractHandler {

    private final ContractRepository contractRepository;

    private final ProjectRepository projectRepository;

    private final TemplateRepository templateRepository;

    private final NoticeRepository noticeRepository;

    private final UserRepository userRepository;

    private final DepartmentRepository departmentRepository;

    public ContractHandler(ContractRepository contractRepository, ProjectRepository projectRepository, TemplateRepository templateRepository, NoticeRepository noticeRepository, UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.contractRepository = contractRepository;
        this.projectRepository = projectRepository;
        this.templateRepository = templateRepository;
        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * 列出项目下所有合同
     */
    public Mono<ServerResponse> listContract(ServerRequest request) {
        log.info("listContract");
        String pid = request.pathVariable("pid");

        return projectRepository.findById(pid)
                .switchIfEmpty(Mono.error(new CommonException(404, "pid 无效")))
                .map(Project::getContracts)
                .flux()
                .flatMap(list -> Flux.fromStream(list.stream()))
                .flatMap(contractUnit -> contractRepository.findById(contractUnit.getCid()))
                .collectList()
                .flatMap(contracts -> ok().contentType(APPLICATION_JSON).bodyValue(contracts));
    }

    /**
     * 上传临时合同文件，保存到数据库，所属project默认为temp
     */
    public Mono<ServerResponse> uploadTempContract(ServerRequest request) {
        log.info("uploadTempContract");
        User user = (User) request.attribute("user_info").get();
        String pid = "temp";

        return request.multipartData()
                // 获取文件部分信息
                .map(map -> map.get("file").get(0))
                .flatMap(part -> {
                    Contract emptyContract = new Contract();

                    // 设置默认基本信息
                    Contract.BaseInfo baseInfo = new Contract.BaseInfo();
                    String fileName = ((FilePart) part).filename();
                    int pointIndex = fileName.lastIndexOf(".");
                    baseInfo.setName(fileName.substring(0, pointIndex));
                    baseInfo.setType(fileName.substring(pointIndex + 1));
                    baseInfo.setProject(pid);

                    //设置创建者信息
                    emptyContract.setCreateInfo(user);
                    emptyContract.setBaseInfo(baseInfo);

                    return contractRepository.save(emptyContract)
                            // 拿到记录实体
                            .flatMap(contract -> Office
                                    // 上传office服务器
                                    .upload(part, contract.getCid())
                                    // 判断上传情况
                                    .filter(s -> !s.contains("filename"))
                                    .flatMap(s -> Mono.error(new CommonException(501, "中奖了，文件上传失败 " + s)))
                                    .defaultIfEmpty(contract)
                                    .map(obj -> (Contract) obj)
                            );
                })
                .doOnNext(contract -> {
                    // 设置uri
                    contract.setUri(Contract.BASE_URI + contract.getCid());
                    // 添加历史追踪
                    contract.getHistories().add(new Contract.History(contract.getCid(), Contract.History.SYSTEM_TYPE,
                            "上传临时合同文件")
                            .setModifier(user));
                })
                // 保存信息
                .flatMap(contractRepository::save)
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * 上传合同
     * 仅仅上传
     * 设置uri
     * 设置创建者信息
     * 设置默认基本信息
     * 添加一条历史记录
     */
    public Mono<ServerResponse> uploadContract(ServerRequest request) {
        log.info("uploadContract");
        User user = (User) request.attribute("user_info").get();
        String pid = request.pathVariable("pid");

        return request.multipartData()
                // 获取文件部分信息
                .map(map -> map.get("file").get(0))
                .flatMap(part -> {
                    Contract emptyContract = new Contract();

                    // 设置默认基本信息
                    Contract.BaseInfo baseInfo = new Contract.BaseInfo();
                    String fileName = ((FilePart) part).filename();
                    int pointIndex = fileName.lastIndexOf(".");
                    baseInfo.setName(fileName.substring(0, pointIndex));
                    baseInfo.setType(fileName.substring(pointIndex + 1));
                    baseInfo.setProject(pid);

                    //设置创建者信息
                    emptyContract.setCreateInfo(user);
                    emptyContract.setBaseInfo(baseInfo);

                    return contractRepository.save(emptyContract)
                            // 拿到记录实体
                            .flatMap(contract -> Office
                                    // 上传office服务器
                                    .upload(part, contract.getCid())
                                    // 判断上传情况
                                    .filter(s -> !s.contains("filename"))
                                    .flatMap(s -> Mono.error(new CommonException(501, "中奖了，文件上传失败 " + s)))
                                    .defaultIfEmpty(contract)
                                    .map(obj -> (Contract) obj)
                            );
                })
                .doOnNext(contract -> {
                    // 设置uri
                    contract.setUri(Contract.BASE_URI + contract.getCid());
                    // 添加历史追踪
                    contract.getHistories().add(new Contract.History(contract.getCid(), Contract.History.SYSTEM_TYPE,
                            "上传合同文件")
                            .setModifier(user));
                })
                // 保存信息
                .flatMap(contractRepository::save)
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * copy一份模板
     * 模板使用数量+1
     * 从模板库创建合同
     * 仅仅创建
     * 设置创建者信息
     * 继承模板相关信息
     * 添加一条历史记录
     */
    public Mono<ServerResponse> createContract(ServerRequest request) {
        log.info("createContract");
        User user = (User) request.attribute("user_info").get();
        String pid = request.pathVariable("pid");
        String tid = request.queryParam("template").orElse("");

        if (tid.equals(""))
            return Mono.error(new CommonException(400, "请提供模板id"));

        return templateRepository.findById(tid)
                .switchIfEmpty(Mono.error(new CommonException(404, "模板id无效")))
                .doOnNext(Template::addApply)
                .flatMap(templateRepository::save)
                .map(template -> {
                    Contract contract = new Contract();
                    //基本信息
                    contract.getBaseInfo().setName(template.getName());
                    contract.getBaseInfo().setType(template.getType());
                    contract.getBaseInfo().setProject(pid);
                    //创建者信息
                    contract.setCreateInfo(user);
                    return contract;
                })
                // 创建合同
                .flatMap(contractRepository::save)
                .flatMap(contract -> Office
                        // office服务器copy操作
                        .copyTemplate(tid, contract.getCid())
                        // 虽然会主动抛出异常,还是过滤哈吧
                        .filter(Boolean::booleanValue)
                        .thenReturn(contract))
                // 添加历史记录
                .doOnNext(contract -> contract.getHistories().add(new Contract.History(contract.getCid(), Contract.History.SYSTEM_TYPE,
                        "从模板 " + contract.getBaseInfo().getName() + " 创建合同")
                        .setModifier(user)))
                // 设置uri
                .doOnNext(contract -> contract.setUri(Contract.BASE_URI + contract.getCid()))
                // 更新
                .flatMap(contractRepository::save)
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * 确认添加合同到项目
     * 保存基本信息
     * 项目添加一条历史记录
     * 合同添加一条历史记录
     * 正式开始编辑
     * 设置handler
     * 设置status
     */
    public Mono<ServerResponse> confirmAddContract(ServerRequest request) {
        log.info("confirmAddContract");
        User user = (User) request.attribute("user_info").get();
        String pid = request.pathVariable("pid");
        String cid = request.queryParam("contract").orElse("");

        if (cid.equals(""))
            return Mono.error(new CommonException(400, "确定提供合同id"));

        return checkCidAndPid(cid, pid)
                .then(request.bodyToMono(Contract.BaseInfo.class))
                // 添加到项目
                .flatMap(baseInfo -> projectRepository
                        .findById(pid)
                        // 设置记录
                        .doOnNext(project -> project.getHistory().add(new Project.HistoryUnit(user.getName() + "新增了合同" + baseInfo.getName())))
                        // 添加基本信息
                        .doOnNext(project -> project.getContracts().add(new Project.ContractUnit(cid, baseInfo)))
                        .flatMap(projectRepository::save))
                // 更新合同信息
                .flatMap(project -> contractRepository
                        .findById(cid)
                        // 同步基本信息
                        .doOnNext(contract -> contract.setBaseInfo(project.getLastContract(cid).getBaseInfo()))
                        // 添加历史记录
                        .doOnNext(contract -> contract
                                .getHistories()
                                .add(new Contract.History(contract.getCid(), Contract.History.SYSTEM_TYPE,
                                        "添加合同到项目")
                                        .setModifier(user)))
                        // 设置status
                        .doOnNext(contract -> contract.setStatus(Contract.EDIT_STATUS))
                        // 设置handler
                        .doOnNext(contract -> contract.setHandler(project.getDrafter().getDid()))
                        .flatMap(contractRepository::save))
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * 回退版本
     * 删除最新版本
     * 项目添加一条历史记录
     * 合同添加一条历史记录
     */
    public Mono<ServerResponse> rollbackContract(ServerRequest request) {
        log.info("rollbackContract");
        User user = (User) request.attribute("user_info").get();
        String pid = request.pathVariable("pid");
        String oldCid = request.queryParam("rollback2").orElse("");
        String lastCid = request.queryParam("lastCid").orElse("");

        if (oldCid.equals("") | lastCid.equals(""))
            return Mono.error(new CommonException(400, "请确定提供合同id"));

        return checkCidAndPid(oldCid, pid)
                .then(projectRepository.findById(pid))
                .flatMap(project -> contractRepository
                        .findById(oldCid)
                        .flatMap(oldContract -> {
                            Project.ContractUnit contractUnit = project.getLastContract(lastCid);
                            // 校验最近版本是否使用中
                            if (!Objects.nonNull(contractUnit))
                                return Mono.error(new CommonException(404, "lastCid无效"));
                            Contract.BaseInfo lastBaseInfo = contractUnit.getBaseInfo();
                            // 项目记录
                            project.getHistory().add(new Project.HistoryUnit(user.getName() + "回退了合同 " + lastBaseInfo.getName() + " 的版本"));
                            // 项目删除当前版本
                            project.getContracts().remove(contractUnit);
                            // 项目添加历史版本
                            project.getContracts().add(new Project.ContractUnit(oldCid, oldContract.getBaseInfo()));
                            // 合同添加历史记录
                            oldContract.getHistories().add(new Contract.History(lastCid, Contract.History.SYSTEM_TYPE,
                                    "回退了合同 " + lastBaseInfo.getName() + " ，使用本版本覆盖")
                                    .setModifier(user)
                            );
                            // 当前合同作废
                            return contractRepository.UpdateStatusByCid(lastCid, Contract.TRASH_STATUS)
                                    .then(contractRepository.save(oldContract));
                        })
                        .then(projectRepository.save(project)))
                .flatMap(project -> ok().contentType(APPLICATION_JSON).bodyValue(project));
    }

    /**
     * 获取指定合同数据
     */
    public Mono<ServerResponse> getContract(ServerRequest request) {
        log.info("getContract");
        String cid = request.pathVariable("cid");

        return contractRepository.findById(cid)
                .switchIfEmpty(Mono.error(new CommonException(404, "cid 无效")))
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * 申请编辑合同
     * 创建新版本
     * 返回基本配置以及合同信息
     */
    public Mono<ServerResponse> applyToEdit(ServerRequest request) {
        log.info("applyToEdit");
        User user = (User) request.attribute("user_info").get();
        String pid = request.pathVariable("pid");
        String cid = request.pathVariable("cid");

        return this.checkCidAndPid(cid, pid)
                .then(contractRepository.findById(cid))
                // 创建新版本
                .flatMap(this::createNewVersionContract)
                // 获取配置
                .flatMap(contract -> Office
                        .applyToEdit(contract.getCid(), contract.getBaseInfo().getName() + "." + contract.getBaseInfo().getType(), "edit", user.getUid(), user.getName())
                        .map(s -> new ApplyToEditResult(contract, s))
                )
                .flatMap(result -> ok().contentType(APPLICATION_JSON).bodyValue(result));
    }

    /**
     * 申请审核合同
     * 返回基本配置
     */
    public Mono<ServerResponse> applyToAudit(ServerRequest request) {
        log.info("applyToAudit");
        User user = (User) request.attribute("user_info").get();
        String cid = request.pathVariable("cid");

        return contractRepository.findById(cid)
                .switchIfEmpty(Mono.error(new CommonException(404, "cid无效")))
                .flatMap(contract -> Office
                        .applyToEdit(contract.getCid(), contract.getBaseInfo().getName() + "." + contract.getBaseInfo().getType(), "review", user.getUid(), user.getName()))
                .flatMap(s -> ok().contentType(APPLICATION_JSON).bodyValue(s));
    }

    /**
     * 保存合同修改信息
     * 项目更新合同版本
     */
    public Mono<ServerResponse> addEditInfo(ServerRequest request) {
        log.info("addEditInfo");
        User user = (User) request.attribute("user_info").get();
        String pid = request.pathVariable("pid");
        String cid = request.pathVariable("cid");

        return this.checkCidAndPid(cid, pid)
                .then(contractRepository.findById(cid))
                .flatMap(contract -> request
                        .bodyToMono(AddEditInfoUnit.class)
                        .flatMap(unit -> projectRepository.findById(pid)
                                // 项目更新合同版本
                                .filter(project -> project.coverContract(unit.getOldCid(), contract.getCid()))
                                .flatMap(projectRepository::save)
                                .thenReturn(contract)
                                // 保存合同修改信息
                                .doOnNext(c -> contract
                                        .getHistories()
                                        .add(new Contract.History(contract.getCid(), Contract.History.EDIT_TYPE, unit.getInfo())
                                                .setModifier(user))
                                )
                                .flatMap(contractRepository::save)))
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * 作废合同
     * 合同添加作废记录
     * 项目添加销毁记录
     */
    public Mono<ServerResponse> deleteContract(ServerRequest request) {
        log.info("deleteContract");
        User user = (User) request.attribute("user_info").get();
        String pid = request.pathVariable("pid");
        String cid = request.pathVariable("cid");

        return this.checkCidAndPid(cid, pid)
                // 合同添加作废记录
                .then(contractRepository.findById(cid))
                .doOnNext(contract -> contract.setStatus(Contract.TRASH_STATUS))
                .doOnNext(contract -> contract
                        .getHistories()
                        .add(new Contract.History(contract.getCid(), Contract.History.SYSTEM_TYPE,
                                "本合同被废弃")
                                .setModifier(user)
                        ))
                // 项目添加销毁记录
                .flatMap(contract -> projectRepository
                        .findById(pid)
                        .doOnNext(project -> project.getHistory().add(new Project.HistoryUnit(user.getName() + " 销毁了合同 " + contract.getBaseInfo().getName())))
                        .flatMap(projectRepository::save)
                        .thenReturn(contract)
                )
                .flatMap(contractRepository::save)
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * 添加审核意见
     * 添加历史记录
     */
    public Mono<ServerResponse> addAuditInfo(ServerRequest request) {
        log.info("addAuditInfo");
        User user = (User) request.attribute("user_info").get();
        String cid = request.pathVariable("cid");

        return request.bodyToMono(AddInfoUnit.class)
                .flatMap(unit -> contractRepository
                        .findById(cid)
                        .switchIfEmpty(Mono.error(new CommonException(404, "cid 无效")))
                        .doOnNext(contract -> contract.getHistories()
                                .add(new Contract.History(contract.getCid(), Contract.History.AUDIT_TYPE, unit)
                                        .setModifier(user)
                                )))
                .flatMap(contractRepository::save)
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * 回到编辑环节
     * 新增版本
     * 项目内修改合同版本
     * 添加合同的历史记录
     * 设置status
     * 设置handler
     */
    public Mono<ServerResponse> moveToEdit(ServerRequest request) {
        log.info("moveToEdit");
        User user = (User) request.attribute("user_info").get();
        String pid = request.pathVariable("pid");
        String cid = request.pathVariable("cid");

        return this.checkCidAndPid(cid, pid)
                .then(contractRepository.findById(cid))
                // 创建新版本
                .flatMap(this::createNewVersionContract)
                .flatMap(contract -> projectRepository.findById(pid)
                        // 项目内修改合同版本
                        .doOnNext(project -> project.coverContract(cid, contract.getCid()))
                        .flatMap(projectRepository::save)
                        .map(Project::getDrafter)
                        .map(drafter -> {
                            // 更新合同信息
                            contract.setStatus(Contract.EDIT_STATUS);
                            contract.setHandler(drafter.getDid());
                            contract.getHistories().add(new Contract.History(contract.getCid(), Contract.History.FLOW_TYPE,
                                    "退回重新拟定")
                                    .setModifier(user)
                            );
                            return contract;
                        })
                        .flatMap(contractRepository::save))
                // 通知编辑部门重修
                .flatMap(contract -> noticeRepository
                        .save((new Notice("system", "html", "合同 <a data-cid='" + contract.getCid() + "' class='$class$' href='$link$'>" + contract.getBaseInfo().getName() + "</a> 已被退回，请求参考审核意见重修")))
                        .flatMap(notice -> this
                                .findAllDepartments(contract.getHandler())
                                .flux()
                                // 查询所有部门有关人员
                                .flatMap(userRepository::findAllInProjects)
                                // 绑定通知
                                .doOnNext(u -> u.getNotice().add(notice.getNid()))
                                .flatMap(userRepository::save)
                                .collectList()
                                .thenReturn(notice)
                        )
                        .thenReturn(contract)
                )
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * 进入下一个步骤
     * 新增版本
     * 项目内修改合同版本
     * 添加合同的历史记录
     * 设置status
     * 设置handler
     * 合同完成时，添加项目历史记录
     */
    public Mono<ServerResponse> moveToNext(ServerRequest request) {
        log.info("moveToNext");
        User user = (User) request.attribute("user_info").get();
        String pid = request.pathVariable("pid");
        String cid = request.pathVariable("cid");

        return this.checkCidAndPid(cid, pid)
                .then(contractRepository.findById(cid))
                // 创建新版本
                .flatMap(this::createNewVersionContract)
                .flatMap(contract -> projectRepository.findById(pid)
                        .doOnNext(project -> {
                            // 更新合同信息
                            Project.UpdateInfoResult result = project.getNextUpdateInfo(cid, contract.getStatus(), contract.getHandler());
                            contract.setStatus(result.getStatus());
                            contract.setHandler(result.getHandler());
                            contract.getHistories().add(new Contract.History(contract.getCid(), Contract.History.FLOW_TYPE,
                                    result.getHistoryInfo())
                                    .setModifier(user)
                            );
                            // 项目内修改合同版本
                            project.coverContract(cid, contract.getCid());
                            if (result.isCompleted()) {
                                project.getHistory().add(new Project.HistoryUnit("合同" + contract.getBaseInfo().getName() + "已完成拟稿与审核"));
                            }
                        })
                        .flatMap(projectRepository::save)
                        .thenReturn(contract)
                        .flatMap(contractRepository::save))
                // 通知下一个流程的部门处理合同
                .flatMap(contract -> noticeRepository
                        .save((new Notice("system", "html", "合同 <a data-cid='" + contract.getCid() + "' class='$class$' href='$link$'>" + contract.getBaseInfo().getName() + "</a> " + (contract.getStatus() == Contract.COMPLETE_STATUS ? "已完成拟稿与审核工作" : "等待您的审核"))))
                        .flatMap(notice -> contract.getStatus() == Contract.COMPLETE_STATUS
                                ?
                                // 同志负责人
                                userRepository
                                        .findById(contract.getHandler())
                                        .doOnNext(u -> u.getNotice().add(notice.getNid()))
                                        .flatMap(userRepository::save)
                                        .thenReturn(notice)
                                :
                                // 通知部门
                                this
                                        .findAllDepartments(contract.getHandler())
                                        .flux()
                                        // 查询所有部门有关人员
                                        .flatMap(userRepository::findAllInProjects)
                                        // 绑定通知
                                        .doOnNext(u -> u.getNotice().add(notice.getNid()))
                                        .flatMap(userRepository::save)
                                        .collectList()
                                        .thenReturn(notice)
                        )
                        .thenReturn(contract)
                )
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * 重命名合同
     * 合同历史添加一条记录
     */
    public Mono<ServerResponse> rename(ServerRequest request) {
        log.info("rename");
        User user = (User) request.attribute("user_info").get();
        String pid = request.pathVariable("pid");
        String cid = request.pathVariable("cid");

        return this.checkCidAndPid(cid, pid)
                .then(request.bodyToMono(RenameUnit.class))
                .flatMap(unit -> contractRepository.findById(cid)
                        .doOnNext(contract -> contract.getHistories().add(new Contract.History(contract.getCid(), Contract.History.EDIT_TYPE,
                                user.getName() + " 重命名合同 " + contract.getBaseInfo().getName() + " 至 " + unit.getName())
                                .setModifier(user))
                        )
                        .doOnNext(contract -> contract.getBaseInfo().setName(unit.getName()))
                        .flatMap(contractRepository::save)
                )
                .flatMap(contract -> ok().contentType(APPLICATION_JSON).bodyValue(contract));
    }

    /**
     * 下载合同文件，进过签名标记可验签
     */
    public Mono<ServerResponse> download(ServerRequest request) {
        log.info("download");
        User user = (User) request.attribute("user_info").get();
        String cid = request.pathVariable("cid");
        String pid = request.pathVariable("pid");
        String fullFilename = request.queryParam("filename").get();

        String url = "http://legal.tgozzz.cn/office/files/__ffff_127.0.0.1/" + cid;

        return this.checkCidAndPid(cid, pid)
                .thenReturn(fullFilename)
                .filter(s -> !s.contains("docx"))
                .thenReturn(new Office.DocxConvertUnit(cid, fullFilename, url))
                .flatMap(Office::downloadAsDocx)
                .switchIfEmpty(Office.download(url))
                .flatMap(bfile -> contractRepository
                        .findById(cid)
                        .flatMap(contract -> projectRepository
                                .findById(pid)
                                .flatMap(project -> createSignature(user, contract, project, bfile))
                                .map(sign -> WordUtils.addWeiFanSignature(bfile, sign))))
                .flatMap(bfile -> ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fullFilename)
                        .contentType(APPLICATION_OCTET_STREAM)
                        .bodyValue(bfile));
    }

    /**
     * 核验签名有效性
     */
    public Mono<ServerResponse> verify(ServerRequest request) {
        log.info("verify");

        String cid = request.pathVariable("cid");

        return contractRepository.findById(cid)
                .switchIfEmpty(Mono.error(new CommonException(404, "cid 无效")))
                .map(Contract::getUri)
                .flatMap(Office::download)
                .map(bfile -> {
                    String summary = SecurityUtils.getSHA256(WordUtils.getContent(bfile) + "文档最终负责人（微泛）：");
                    String jsonBody = WordUtils.resolveWeiFanSignature(bfile);
                    ObjectMapper mapper = new ObjectMapper();
                    SignatureBody body = null;

                    try {
                        body = mapper.readValue(jsonBody, SignatureBody.class);
                    } catch (JsonProcessingException ignored) {
                    }

                    if (!summary.equals(body.getSummary())) {
                        body.setSummary("");
                    }

                    return body;
                })
                .flatMap(signatureBody -> ok().contentType(APPLICATION_JSON).bodyValue(signatureBody));
    }

    /**
     * 校验id参数 是否有效
     * 无效抛出异常
     */
    public Mono<Boolean> checkCidAndPid(String cid, String pid) {

        return projectRepository
                .existsById(pid)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new CommonException(404, "pid无效")))
                .then(contractRepository.existsById(cid))
                .switchIfEmpty(Mono.error(new CommonException(404, "contract无效")))
                .thenReturn(true);
    }

    /**
     * 创建新的合同版本
     * 设置uri
     */
    public Mono<Contract> createNewVersionContract(Contract oldCon) {
        String oldCid = oldCon.getCid();
        oldCon.setCid(null);

        return contractRepository.save(oldCon)
                .flatMap(newCon -> Office
                        .copyTemplate(oldCid, newCon.getCid())
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new CommonException(501, "新版本尝试创建但是服务器存储失败")))
                        .thenReturn(newCon))
                .doOnNext(contract -> contract.setUri(Contract.BASE_URI + "/" + contract.getCid()));
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
     * 将InputStream写入本地文件
     *
     * @param destination 写入本地目录
     * @param input       输入流
     * @throws IOException IOException
     */
    public static void writeToLocal(String destination, InputStream input)
            throws IOException {
        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destination);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        input.close();
        downloadFile.close();

    }

    @SneakyThrows
    public Mono<byte[]> createSignature(User u, Contract c, Project p, byte[] bfile) {
        SignatureBody body = new SignatureBody();
        body.setLoadTime(new Date().getTime());
        body.setLoaderName(u.getName());
        body.setLoaderId(u.getUid());
        body.setContractName(c.getBaseInfo().getName());
        body.setCid(c.getCid());
        body.setProjectName(p.getBaseInfo().getName());
        body.setPid(p.getPid());
        body.setModifierName(c.getHistories().get(c.getHistories().size() - 1).getModifierName());
        body.setModifierId(c.getHistories().get(c.getHistories().size() - 1).getModifierUid());
        body.setSummary(SecurityUtils.getSHA256(WordUtils.getContent(bfile)));

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(body);

        return userRepository
                .findById(body.getModifierId())
                .map(user -> user.getSigns().get(0))
                .map(ImageUtils::base64ToByte)
                .map(ImageUtils.Png::incise)
                .doOnNext(png -> png.setIEXT(jsonBody))
                .map(ImageUtils.Png::getBytes);
    }

    @Data
    @NoArgsConstructor
    public static class AddInfoUnit {
        private String summary = "";
        private String details = "";
    }

    @Data
    @NoArgsConstructor
    public static class AddEditInfoUnit {
        private String oldCid = "";
        private AddInfoUnit info = new AddInfoUnit();
    }

    @Data
    @NoArgsConstructor
    public static class CreateContractUnit {
        private String template = "";
    }

    @Data
    @NoArgsConstructor
    public static class RenameUnit {
        private String name = "";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyToEditResult {
        private Contract contract;
        private String config;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignatureBody {
        private long loadTime;
        private String loaderName;
        private String loaderId;
        private String contractName;
        private String cid;
        private String modifierName;
        private String modifierId;
        private String projectName;
        private String pid;
        private String summary;
    }
}
