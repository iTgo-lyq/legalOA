package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.domain.Word;
import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.repository.WordRepository;
import cn.tgozzz.legal.utils.WordUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.reactive.function.server.ServerResponse.permanentRedirect;

@Log4j2
@Component
public class WordHandler {

    private final WordRepository repository;

    WordHandler(WordRepository repository) {
        this.repository = repository;
    }

    /**
     * 获取所有文档
     */
    public Mono<ServerResponse> getAllWords(ServerRequest request) {
        return repository
                .findAll().collectList()
                .flatMap(words -> ok().bodyValue(words));
    }

    /**
     * 获取指定类型word文档，默认word
     */
    public Mono<ServerResponse> getWord(ServerRequest request) {
        log.info("getWord");
        String wid = request.pathVariable("wid");
        String media = request.queryParam("media").or(() -> Optional.of("word")).get();

        return repository.findById(wid)
                // 检查指定格式地址是否存在，不存在则创建
                .flatMap(word -> {
                    String path;
                    try {
                        switch (media) {
                            case "word":
                                path = word.getWordPath();
                                if(path.equals("")) {
                                    WordUtils.transform(word.getWid(), "html", "doc");
                                    word.updateWordPath();
                                }
                                break;
                            case "html":
                                path = word.getHtmlPath();
                                if(path.equals("")){
                                    String[] ary = word.getWordPath().split("\\.");
                                    String originMedia = ary[ary.length - 1];
                                    if(originMedia.equals("docx"))
                                        WordUtils.transform(word.getWid(), "docx", "html");
                                    if(originMedia.equals("doc"))
                                        WordUtils.transform(word.getWid(), "doc", "html");
                                    word.updateHtmlPath();
                                }
                                break;
                            case "pdf":
                                path = word.getPdfPath();
                                return Mono.error(new CommonException("该格式暂不支持"));
                            case "image":
                                path = word.getHtmlPath();
                                return Mono.error(new CommonException("该格式暂不支持"));
                            default:
                                return Mono.error(new CommonException("请检查格式"));
                        }
                    } catch (ParserConfigurationException | TransformerException | IOException  e) {
                        return Mono.error(new CommonException("文件转换出错 " + e.getMessage()));
                    }
                    return repository.save(word);
                })
                // 返回对应格式地址
                .map(word -> {
                    switch (media){
                        case "word":
                            return word.getWordPath();
                        case "html":
                            return word.getHtmlPath();
                        case "pdf":
                            return word.getPdfPath();
                        case "image":
                            return word.getImagePath();
                    }
                    return null;
                })
                // 重定向到资源路径
                .flatMap(path -> permanentRedirect(URI.create(path)).build());
    }

    /**
     * 存储word文件
     */
    public Mono<ServerResponse> uploadWord(ServerRequest request) {
        log.info("uploadWord");
        return request.multipartData()
                .map(MultiValueMap::toSingleValueMap)
                .flatMap(map -> {
                    FilePart sourceFile;
                    try {
                        sourceFile = (FilePart) map.get("file");
                    }catch (Exception e) {
                        return Mono.error(new CommonException("无文件"));
                    }

                    Map<String, String> fileNameMap = WordUtils.resolveName(sourceFile.filename());
                    String fileName = fileNameMap.get("name");
                    String media = fileNameMap.get("media");

                    if(!media.equals("doc") && !media.equals("docx"))
                        return Mono.error(new CommonException("文件类型错误"));

                    return repository.save(new Word(fileName))
                            .flatMap(word -> {
                                Path targetFile;
                                try {
                                    targetFile = Files.createFile(Paths.get(URI.create("file://" + WordUtils.baseWordPath + word.getWid() + "." + media)));
                                } catch (IOException e) {
                                    return Mono.error(new CommonException("存储失败" + e.getMessage()));
                                }
                                sourceFile
                                        .transferTo(targetFile)
                                        .onErrorResume(IllegalStateException.class,
                                                e -> Mono.error(new CommonException("文件无效" + e.getMessage())));
                                return Mono.just(word);
                            })
                            // 生成网络路径
                            .doOnNext(word -> word.updateWordPath(media))
                            // 初始化文档历史记录
                            .doOnNext(word -> word.addLog("————创建————"))
                            // 更新网络路径
                            .flatMap(repository::save);
                        })
                .flatMap(word -> ok().bodyValue(word));
    }

    /**
     * 存储html文本
     */
    public Mono<ServerResponse> uploadHtmlDoc(ServerRequest request) {
        log.info("uploadHtmlDoc");
        String fileName = request.queryParam("filename").orElse("未命名");

        return repository
                // 创建wid
                .save(new Word(fileName))
                .flatMap(word-> request.bodyToMono(String.class)
                        // 存储文件
                        .flatMap(str ->{
                            byte[] bytes = str.getBytes();
                            Path path = Paths.get(URI.create("file://" + WordUtils.baseHtmlDocPath + word.getWid() + ".html"));
                            File file = path.toFile();
                            try {
                            if(!file.exists())
                                file.createNewFile();
                                return Mono.just(Files.write(path, bytes)); //覆盖模式
                            } catch (IOException e) {
                                return Mono.error(new CommonException("文件读写错误 " + e.getMessage()));
                            }
                        })
                        // 生成网络路径
                        .doOnNext(path ->
                                word.updateHtmlPath())
                        // 初始化文档历史记录
                        .doOnNext(path ->
                                word.addLog("————创建————"))
                        // 更新网络路径
                        .flatMap(path ->
                                repository.save(word)
                        )
                        .switchIfEmpty(Mono.error(new CommonException("内容为空"))))
                .flatMap(word -> ok().bodyValue(word));
    }

    /**
     * 存储新版本word， 增加版本记录
     */
    public Mono<ServerResponse> updateWord(ServerRequest request) {
        log.info("updateWord");

        String wid = request.pathVariable("wid");
        String log = request.queryParam("log").or(()-> Optional.of("更新文档")).get();

        return request.multipartData()
                .map(MultiValueMap::toSingleValueMap)
                .flatMap(map -> {
                    FilePart sourceFile;
                    try {
                        sourceFile = (FilePart) map.get("file");
                    }catch (Exception e) {
                        return Mono.error(new CommonException("无文件"));
                    }

                    Map<String, String> fileNameMap = WordUtils.resolveName(sourceFile.filename());
                    String fileName = fileNameMap.get("name");
                    String media = fileNameMap.get("media");

                    if(!media.equals("doc") && !media.equals("docx"))
                        return Mono.error(new CommonException("文件类型错误"));

                    return repository
                            .findById(wid)
                            .switchIfEmpty(Mono.error(new CommonException("文件未存储，文档id无效 " + wid)))
                            .doOnNext(word -> {
                                word.clearPath();
                                word.addLog(log); // 增加log之前需要wid
                                word.setWid(null);
                                word.updateWordPath(media);
                                word.setName(fileName);
                            })
                            .flatMap(repository::save)
                            .flatMap(word -> {
                                Path targetFile;
                                try {
                                    targetFile = Files.createFile(Paths.get(URI.create("file://" + WordUtils.baseWordPath + word.getWid() + "." + media)));
                                } catch (IOException e) {
                                    return Mono.error(new CommonException("存储失败" + e.getMessage()));
                                }
                                sourceFile
                                        .transferTo(targetFile)
                                        .onErrorResume(IllegalStateException.class,
                                                e -> Mono.error(new CommonException("文件无效" + e.getMessage())));
                                return Mono.just(word);
                            })
                            // 生成网络路径
                            .doOnNext(word -> word.updateWordPath(media))
                            // 更新网络路径
                            .flatMap(repository::save);
                })
                .flatMap(word -> ok().bodyValue(word));
    }

    /**
     * 存储新版本html， 增加版本记录
     */
    public Mono<ServerResponse> updateHtmlDoc(ServerRequest request) {
        log.info("updateHtmlDoc");

        String wid = request.pathVariable("wid");
        Optional<String> fileName = request.queryParam("filename");
        String log = request.queryParam("log").or(()-> Optional.of("更新文档")).get();

        return request.bodyToMono(String.class)
                .flatMap(str -> {
                    byte[] bytes = str.getBytes();
                    Path path = Paths.get(URI.create("file://" + WordUtils.baseHtmlDocPath + wid + ".html"));
                    File file = path.toFile();
                    try {
                        if(!file.exists())
                            file.createNewFile();
                        return Mono.just(Files.write(path, bytes)); //覆盖模式
                    } catch (IOException e) {
                        return Mono.error(new CommonException("文件读写错误 " + e.getMessage()));
                    }
                })
                .flatMap(path -> repository.findById(wid))
                .doOnNext(word -> {
                    word.clearPath();
                    word.addLog(log); // 增加log之前需要wid
                    word.setWid(null);
                    word.setName(fileName.or(()-> Optional.ofNullable(word.getName())).get());
                    word.updateHtmlPath();
                })
                .switchIfEmpty(Mono.error(new CommonException("文件未存储，文档id无效 " + wid)))
                .flatMap(repository::save)
                .flatMap(word -> ok().bodyValue(word));
    }

    /**
     * 强制覆盖word文件
     */
    public Mono<ServerResponse> coverWord(ServerRequest request) {
        log.info("coverWord");
        String wid = request.pathVariable("wid");
        return request.multipartData()
                .map(MultiValueMap::toSingleValueMap)
                .flatMap(map -> {
                    // 存储文件
                    FilePart sourceFile;
                    try {
                        sourceFile = (FilePart) map.get("file");
                    }catch (Exception e) {
                        return Mono.error(new CommonException("无文件"));
                    }

                    Map<String, String> fileNameMap = WordUtils.resolveName(sourceFile.filename());
                    String fileName = fileNameMap.get("name");
                    String media = fileNameMap.get("media");

                    if(!media.equals("doc") && !media.equals("docx"))
                        return Mono.error(new CommonException("文件类型错误"));

                    Path targetFile;

                    try {
                        targetFile = Files.createFile(Paths.get(URI.create("file://" + WordUtils.baseWordPath + wid + "." + media)));
                    } catch (IOException e) {
                        return Mono.error(new CommonException("存储失败" + e.getMessage()));
                    }

                    sourceFile
                            .transferTo(targetFile)
                            .onErrorResume(IllegalStateException.class,
                                    e -> Mono.error(new CommonException("文件无效" + e.getMessage())));
                    // 更新数据库各类文件路径
                    return repository.findById(wid)
                            .doOnNext(word -> {
                                word.setName(fileName);
                                word.updateWordPath(media);
                                word.setHtmlPath("");
                                word.setImagePath("");
                                word.setPdfPath("");
                            })
                            .switchIfEmpty(Mono.error(new CommonException("文件已存储，文档id无效 " + wid)))
                            .flatMap(repository::save);
                })
                .flatMap(word -> ok().bodyValue(word));
    }

    /**
     * 强制覆盖html文件
     */
    public Mono<ServerResponse> coverHtmlDoc(ServerRequest request) {
        log.info("coverHtmlDoc");
        String wid = request.pathVariable("wid");
        String fileName = request.queryParam("filename").orElse("未命名");
        return request.bodyToMono(String.class)
                .flatMap(str -> {
                    byte[] bytes = str.getBytes();
                    Path path = Paths.get(URI.create("file://" + WordUtils.baseHtmlDocPath + wid + ".html"));
                    File file = path.toFile();
                    try {
                        if(!file.exists())
                            file.createNewFile();
                        return Mono.just(Files.write(path, bytes)); //覆盖模式
                    } catch (IOException e) {
                        return Mono.error(new CommonException("文件读写错误 " + e.getMessage()));
                    }
                })
                .flatMap(path -> repository.findById(wid))
                .doOnNext(word -> {
                    word.setName(fileName);
                    word.updateHtmlPath();
                    word.setWordPath("");
                    word.setImagePath("");
                    word.setPdfPath("");
                })
                .switchIfEmpty(Mono.error(new CommonException("文件已存储，文档id无效 " + wid)))
                .flatMap(repository::save)
                .flatMap(word -> ok().bodyValue(word));
    }
}
