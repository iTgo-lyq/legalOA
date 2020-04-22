package cn.tgozzz.legal.handler;

import cn.tgozzz.legal.exception.CommonException;
import cn.tgozzz.legal.utils.Ltp;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Log4j2
@Component
public class LtpHandler {

    // 中文分词(cws)、词性标注(pos)、依存句法分析(dp)、命名实体识别(ner)
    // 语义角色标注(srl)、语义依存 (依存树)(sdp) (依存图)(sdgp)、关键词提取(ke)
    private static final ArrayList<String> XunFeiPlatform
            = new ArrayList<>(Arrays.asList("cws", "pos", "dp", "ner", "srl", "sdp", "sdgp", "ke"));

    // 词法分析(lexerCustom)、依存句法分析(depParser)、DNN语言模型(dnnlmCn)
    // 词义相似度(wordSimEmbedding)、短文本相似度(simnet)
    // 文章标签(keyword)、文章分类(topic)、文本纠错(ecnet)、新闻摘要接口(newsSummary)
    private static final ArrayList<String> BaiDuPlatform
            = new ArrayList<>(Arrays.asList("lexerCustom", "depParser", "dnnlmCn", "wordSimEmbedding", "simnet", "keyword", "ecnet", "newsSummary"));

    // 文本纠错(TextCorrection)、句法依存分析(DependencyParsing)、词向量(WordEmbedding)、句向量(SentenceEmbedding)
    // 词相似度(WordSimilarity)、文本相似度 (SentenceSimilarity)、自动摘要(AutoSummarization)、关键词提取(KeywordsExtraction)
    // 文本分类(TextClassification)、相似词(SimilarWords)、词法分析(LexicalAnalysis)
    private static final ArrayList<String> TencentPlatform
            = new ArrayList<>(Arrays.asList("TextCorrection", "DependencyParsing", "WordEmbedding", "SentenceEmbedding", "WordSimilarity",
            "SentenceSimilarity", "AutoSummarization", "KeywordsExtraction", "TextClassification", "SimilarWords", "LexicalAnalysis"));

    public Mono<ServerResponse> LP(ServerRequest request) {
        // 调用接口
        String mode = request.pathVariable("mode");
        log.info("LP :: " + mode);
        return Mono.just(mode)
                .doOnNext(System.out::println)
                .map(LtpHandler::shunt)
                .doOnNext(System.out::println)
                .switchIfEmpty(Mono.error(new CommonException("分析模式错误")))
                .doOnNext(System.out::println)
                .flatMap(m ->
                        request.bodyToMono(String.class)
                                .switchIfEmpty(Mono.error(new CommonException("内容不可为空")))
                                .map(LtpHandler::separate)
                                .flatMap(strs -> {
                                    switch (m) {
                                        case "XunFei":
                                            return Ltp.analyzeByXF(mode, strs[0]);
                                        case "BaiDu":
                                            return Ltp.analyzeByBD(mode, strs);
                                        case "Tencent":
                                            return Ltp.analyzeByTX(mode, strs);
                                    }
                                    return Mono.error(new CommonException("无匹配平台"));
                                })
                )
                .flatMap(res -> ok().bodyValue(res));
    }

    /**
     * 是否非法的mode
     */
    private static String shunt(String mode) {
        for (String m : XunFeiPlatform) {
            if (m.equals(mode)) return "XunFei";
        }
        for (String m : BaiDuPlatform) {
            if (m.equals(mode)) return "BaiDu";
        }
        for (String m : TencentPlatform) {
            if (m.equals(mode)) return "Tencent";
        }
        return null;
    }

    /**
     * 分割参数文本
     */
    private static String[] separate(String origin) {
        String[] data = origin.split("¦-¦");
        for (int i = 0; i < data.length; i++)
            data[i] = data[i].trim();
        return data;
    }
}
