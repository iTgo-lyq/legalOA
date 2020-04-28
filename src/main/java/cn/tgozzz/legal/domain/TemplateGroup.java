package cn.tgozzz.legal.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Document(collection = "templateGroups")
@Data
public class TemplateGroup {

    @Id
    private String tgid;
    private String category = "";
    private String permission = "read";
    private String info = "";
    private String owner = "system";
    private String baseUri = "http://legal.tgozzz.cn/office/files/__ffff_127.0.0.1/";
    private int count = 0;
    private long createTime = new Date().getTime();
    private long updateTime = new Date().getTime();
    private ArrayList<String> updateInfo = new ArrayList<>();
    private ArrayList<String> templates = new ArrayList<>();

    /**
     * 添加修改备注时自动修改最近更新时间
     */
    public void setUpdateInfo(String info) {
        this.updateInfo.add(0, info);
        this.updateTime = new Date().getTime();
    }

    /**
     * 上传添加模板
     */
    public void addTemplate(ArrayList<String> temp, String info) {
        this.templates.addAll(temp);
        this.count += temp.size();
        Date t = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.setUpdateInfo(info + " - " + df.format(t));
    }

    /**
     * 覆盖旧的tid
     * @param tid
     */
    public void coverTemplate(Template tid) {

    }

    public void extendTemplate(Template template) {

    }
}
