package cn.tgozzz.legal.domain;

import cn.tgozzz.legal.utils.GlobalConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "word")
@Data
public class Word {

    @Id
    private String wid;

    private String name = "";

    private String pdfPath = "";
    private String wordPath = "";
    private String htmlPath = "";
    private String imagePath = "";
    private List<ChangeLog> logs = new ArrayList<ChangeLog>();

    public Word(String name) {
        this.name = name;
    }

    public void updateHtmlPath() {
        this.setHtmlPath(GlobalConfig.sourceUrl + "/word/htmlDoc/" + this.getWid() + ".html");
    }

    public void updateWordPath() {
        this.setWordPath(GlobalConfig.sourceUrl + "/word/doc/" + this.getWid() + "." + "doc");
    }

    public void updateWordPath(String media) {
        this.setWordPath(GlobalConfig.sourceUrl + "/word/doc/" + this.getWid() + "." + media);
    }

    public void updatePdfPath() {
        this.setPdfPath(GlobalConfig.sourceUrl + "/word/pdf/" + this.getWid() + ".pdf");
    }

    public void updateImagePath() {
        this.setImagePath(GlobalConfig.sourceUrl + "/word/image/" + this.getWid() + ".jpg");
    }

    public void clearPath() {
        this.setWordPath("");
        this.setPdfPath("");
        this.setHtmlPath("");
        this.setImagePath("");
    }

    /**
     *  增加log之前需要wid
     */
    public void addLog(String msg) {
        this.logs.add(new ChangeLog(msg, this.getWid()));
    }
}

@Data
@AllArgsConstructor
class ChangeLog {
    private String wid;
    private long time;
    private String message;

    ChangeLog() {}

    ChangeLog(String msg, String wid) {
        this.initTime();
        this.setMessage(msg);
        this.setWid(wid);
    }

    private void initTime() {
        this.time = new Date().getTime();
    }
}
