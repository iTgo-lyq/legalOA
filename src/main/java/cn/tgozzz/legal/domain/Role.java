package cn.tgozzz.legal.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
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
    private permission permission = new permission(); // 权限列表

    @Data
    @NoArgsConstructor
    public static class permission{
        private String test = "1";
        private tacoma test2 = new tacoma();

        @Data
        @NoArgsConstructor
        public static class tacoma {
            private String test2 = "哈哈";
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class singlePerm {
        private String label; // 权限对应标签名
        private String webRoute; // 对应前端路由路径
        private String httpRoute; // 对应请求路由路径 e.g. get://, post://
        private boolean on; //是否放行
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class multiplePerm {
        private String label;
        private String webRoute; // 对应前端路由路径
        private String httpRoute; // 对应请求路由路径 e.g. get://, post://
        private boolean on; //是否放行
        private ArrayList<singlePerm> p;
    }
}

