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
     * 添加更新备注时自动修改最近更新时间
     * 备注末尾自动添加当前时间
     */
    public void setUpdateInfo(String info) {
        Date t = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.updateInfo.add(0, info + " - " + df.format(t));
        this.updateTime = t.getTime();
    }

    /**
     * 上传添加模板
     */
    public void addTemplate(ArrayList<String> temp, String info, User user) {
        this.templates.addAll(temp);
        this.count += temp.size();
        this.setUpdateInfo(info);
    }

    /**
     * 覆盖旧的模板
     */
    public void coverTemplate(Template template, User user) {

        ArrayList<String> baseT = template.getBaseT();
        String oldT = baseT.isEmpty() ? "empty" : baseT.get(0);
        String newT = template.getBaseT().get(0);

        for (int i = 0; i < this.templates.size(); i++) {
            if (templates.get(i).equals(oldT)) { //有版本变化，修改信息
                this.setUpdateInfo(" 用户 " + user.getName() + " 更新了模板 " + template.getName());
                this.templates.set(i, template.getTid());
                return;
            } else if (templates.get(i).equals(newT)) { //无版本变化，只是修改信息
                this.setUpdateInfo(" 用户 " + user.getName() + " 更新了模板 " + template.getName());
                return;
            }
        }

        this.templates.add(template.getTid()); //不知道哪里冒出来的
        this.setUpdateInfo(" 用户 " + user.getName() + " 将模板 " + template.getName() + " 转移至本组");
    }

    /**
     * 追加新的的模板
     */
    public void extendTemplate(Template template, User user) {
        this.templates.add(template.getTid());
        this.count++;
        this.setUpdateInfo("用户 " + user.getName() + " 更新了模板 " + template.getName());
    }

    /**
     * 删除模板
     */
    public void deleteTemplate(Template template, User user) {
        for (int i = 0; i < this.templates.size(); i++) {
            if(this.templates.get(i).equals(template.getTid())){
                this.count--;
                this.templates.remove(i);
                this.setUpdateInfo("用户" + user.getName() + " 删除了 模板" + template.getName());
            }
        }
    }

    /**
     * 转移模板
     */
    public void deleteTemplate(Template oldT, TemplateGroup newG, User user) {
        for (int i = 0; i < this.templates.size(); i++) {
            if(this.templates.get(i).equals(oldT.getTid())){
                this.count--;
                this.templates.remove(i);
                this.setUpdateInfo("用户" + user.getName() + " 移动模板 " + oldT.getName() + " 至 " + newG.getCategory());
            }
        }
    }
}
