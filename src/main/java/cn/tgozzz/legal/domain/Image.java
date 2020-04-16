package cn.tgozzz.legal.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "image")
public class Image {

    @Id
    private String pid = "";

    private String url = "";

    private List<String> tag = new ArrayList<>();

    private String group = "";
}
