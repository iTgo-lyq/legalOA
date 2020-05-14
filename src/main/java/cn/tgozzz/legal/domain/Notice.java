package cn.tgozzz.legal.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "notice")
@Data
public class Notice {

    @Id
    private String nid;
    private long createTime = new Date().getTime();
    private String tag = "";
    private String type = "";
    private String content = "";

    public Notice(String tag, String type, String content) {
        this.tag = tag;
        this.type = type;
        this.content = content;
    }
}
