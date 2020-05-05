package cn.tgozzz.legal.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

@Document(collection = "user")
@Data
public class User {

    static String[] portraits_sample = new String[]{"http://qiniu.tgozzz.cn/slime_sample_03.jpg", "http://qiniu.tgozzz.cn/slime_sample_02.jpg", "http://qiniu.tgozzz.cn/slime_sample_01.jpg", "http://qiniu.tgozzz.cn/slime_sample_00.jpg"};

    @Id
    private String uid;
    private String phone = "";

    private int age = 99;

    private Boolean sex = true;

    private String email = "";
    private String name = "";
    private String password = "";
    private String portrait = portraits_sample[(int)(Math.random()*4)];
    private String token = "";
    private long createTime = new Date().getTime();
    private Temp template = new Temp();
    private Organization organization = new Organization();

    /**
     * MD5 加密密码
     */
    @SneakyThrows
    public void setPassword(String pw) {
        try {
            password = DigestUtils.md5DigestAsHex(pw.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 直接设置密码
     */
    public void embedPassword(String pw) {
        this.password = pw;
    }

    /**
     * 隐藏密码
     */
    public String digPassword() {
        return password;
    }
    public String getPassword() {
        return "******";
    }

    /**
     * 收藏合同模板
     */
    public boolean markTemplate(String tid) {
        return template.getMark().add(tid);
    }

    /**
     * 取消收藏合同模板
     */
    public boolean cancelMarkTemplate(String tid) {
        return template.getMark().remove(tid);
    }

    /**
     * 覆盖更新我的模板
     */
    public void updateMineTemp(String oldTid, String newTid) {
        this.template.getMine().remove(oldTid);
        this.template.getMine().add(newTid);
    }

    /**
     * 追加更新我的模板
     */
    public void updateMineTemp(String tid) {
        this.template.getMine().add(tid);
    }

    /**
     * 追加更新我的模板
     */
    public void updateMineTemp(ArrayList<String> list) {
        HashSet<String> mine = this.template.getMine();
        mine.addAll(list);
    }

    /**
     * 如果该模板属于该用户则连带删除
     * 返回true 即 用户信息需要保存
     */
    public boolean deleteTemplate(Template template) {
        if(template.getOwner().equals(this.uid))
            return this.getTemplate().getMine().remove(template.getTid());
        return false;
    }

    @Data
    @NoArgsConstructor
    private static class Temp {
        private HashSet<String> mark = new HashSet<>();
        private HashSet<String> mine = new HashSet<>();
        private HashSet<String> use = new HashSet<>();
    }

    @Data
    @NoArgsConstructor
    public static class Organization {
        private int status = 0;
        private Permission permission = new Permission();
        private ArrayList<String> roles = new ArrayList<>();
        private UserDepartment department = new UserDepartment();

        @Data
        @NoArgsConstructor
        public static class UserDepartment {
            private String did = "";
            private String name = "";
            private boolean leader = false;
        }
    }
}