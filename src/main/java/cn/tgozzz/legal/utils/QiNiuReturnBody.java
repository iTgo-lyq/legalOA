package cn.tgozzz.legal.utils;

import lombok.Data;

@Data
public class QiNiuReturnBody {
    // 绑定域名
    public static String baseUrl = "http://q8tpx3tgs.bkt.clouddn.com/";
    // 返回内容模板
    static String templete = "{\"key\":\"$(key)\",\"name\":\"$(fname)\",\"bucket\":\"$(bucket)\",\"size\":$(fsize)}";

    private String key;
    private String name;
    private Integer size;
    private String bucket;
}
