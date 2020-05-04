package cn.tgozzz.legal.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "role")
@Data
public class Role {
    @Id
    private String rid;
    private String name = ""; // 角色名称
    private String description = ""; // 描述
    private String createBy = ""; // 创建者name
    private String updateBy = ""; // 更新者name
    private int status = 0; // 状态
    private long createTime = new Date().getTime(); // 创建时间
    private long updateTime = new Date().getTime(); // 更新时间
    private Permission permission = new Permission(); // 权限列表
}

