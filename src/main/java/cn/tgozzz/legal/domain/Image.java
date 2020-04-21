package cn.tgozzz.legal.domain;

import cn.tgozzz.legal.utils.QiNiuReturnBody;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "image")
@Data
public class Image {

    @Id
    private String pid;

    private String url = "";

    private String name = "";

    private String group = "";

    private List<String> tags = new ArrayList<>();

    private Integer size = 0;

    private String bucket = "";

    public Image(QiNiuReturnBody res) {
        this.size = res.getSize();
        this.bucket = res.getBucket();
        this.url = QiNiuReturnBody.baseUrl + res.getKey();
        this.name = res.getName();
    }
}
