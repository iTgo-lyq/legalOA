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
    private Boolean on = false; //下级是否放行
    private HashMap<String, PermUnit> children = new HashMap<>();

    public Permission() {
        HashMap<String, PermUnit> userMap = new HashMap<>();
        userMap.put("template", new PermUnit("查阅模板", "/template", "/template", false, null));

        children.put("management", new PermUnit("管理", "/", "/management", false, null));
        children.put("user", new PermUnit("用户", "/user", "/user", false, userMap));
    }

    /**
     * 合并两个Permission
     * P1 将被修改并返回
     */
    public static Permission merge(Permission p1, Permission p2) {
        HashMap<String, PermUnit> p1kids = p1.getChildren() == null ? new HashMap<>() : p1.getChildren();
        HashMap<String, PermUnit> p2kids = p2.getChildren() == null ? new HashMap<>() : p2.getChildren();
        p1.setOn(p1.getOn() | p2.getOn());
        p1kids.forEach((s, unit) ->
                p1kids.put(s, PermUnit.merge(unit, p2kids.get(s)))
        );
        return p1;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermUnit {
        private String label; // 权限对应标签名
        private String webRoute; // 对应前端路由路径
        private String httpRoute; // 对应请求路由路径 e.g. get://, post://
        private Boolean on; //是否放行
        private HashMap<String, PermUnit> children;

        private static PermUnit merge(PermUnit u1, PermUnit u2) {
            HashMap<String, PermUnit> u1kids = u1.getChildren() == null ? new HashMap<>() : u1.getChildren();
            HashMap<String, PermUnit> u2kids = u2.getChildren() == null ? new HashMap<>() : u2.getChildren();
            u1.setOn(u1.getOn() | u2.getOn());
            u1kids.forEach((s, unit) ->
                    u1kids.put(s, PermUnit.merge(unit, u2kids.get(s)))
            );
            return u1;
        }
    }
}
