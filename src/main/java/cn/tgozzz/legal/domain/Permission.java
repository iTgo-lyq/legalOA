package cn.tgozzz.legal.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class Permission {

    private String label = "微泛";
    private String webRoute = "*"; // 对应前端路由路径
    private String httpRoute = "http:*"; // 对应请求路由路径 e.g. get:/, post:/, http:/
    private boolean on; //是否放行
    private HashMap<String, PermUnit> children;

    Permission(){
        HashMap<String, PermUnit> userMap = new HashMap<>();
        userMap.put("template", new PermUnit("查阅模板", "/template", "/template",false, null));

        children.put("management", new PermUnit("管理", "/", "/management", false, null));
        children.put("user", new PermUnit("用户", "/user", "/user", false, userMap));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PermUnit {
        private String label; // 权限对应标签名
        private String webRoute; // 对应前端路由路径
        private String httpRoute; // 对应请求路由路径 e.g. get://, post://
        private boolean on; //是否放行
        private HashMap<String, PermUnit> children;
    }
}
