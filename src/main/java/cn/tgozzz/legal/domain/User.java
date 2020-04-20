package cn.tgozzz.legal.domain;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;

@Document(collection = "user")
@Data
public class User {

    @Id
    private String uid;
    private String phone = "";

    private int age = 99;

    private Boolean sex = true;

    private String email = "";
    private String name = "";
    private String password = "";
    private String portrait = "";
    private String token = "";

    @SneakyThrows
    public void setPassword(String pw) {
        try {
            password = DigestUtils.md5DigestAsHex(pw.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String digPassword() {
        return password;
    }

    public String getPassword() {
        return "******";
    }
}
