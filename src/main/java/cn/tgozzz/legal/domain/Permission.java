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
        HashMap<String, PermUnit> templateMap = new HashMap<>();
        templateMap.put("lookLib", new PermUnit("查阅模板库", "/lookLib", "/lookLib", false, null));
        templateMap.put("createLib", new PermUnit("创建模板库", "/createLib", "/createLib", false, null));
        templateMap.put("deleteLib", new PermUnit("删除模板库", "/deleteLib", "/deleteLib", false, null));
        templateMap.put("lookTemp", new PermUnit("查看模板", "/lookTemp", "/lookTemp", false, null));
        templateMap.put("downloadTemp", new PermUnit("下载模板", "/downloadTemp", "/downloadTemp", false, null));
        templateMap.put("addTemp", new PermUnit("添加模板", "/template", "/template", false, null));
        templateMap.put("updateTemp", new PermUnit("修改模板", "/template", "/template", false, null));
        templateMap.put("moveTemp", new PermUnit("移动模板", "/template", "/template", false, null));
        templateMap.put("deleteTemp", new PermUnit("删除模板", "/template", "/template", false, null));

        HashMap<String, PermUnit> projectMap = new HashMap<>();
        projectMap.put("createProject", new PermUnit("创建项目", "/template", "/template", false, null));
        projectMap.put("deleteProject", new PermUnit("删除项目", "/template", "/template", false, null));
        projectMap.put("stopProject", new PermUnit("停止项目", "/template", "/template", false, null));
        projectMap.put("restartProject", new PermUnit("重启项目", "/template", "/template", false, null));

        HashMap<String, PermUnit> adminMap = new HashMap<>();
        adminMap.put("uploadUser", new PermUnit("导入用户", "/template", "/template", false, null));
        adminMap.put("addUser", new PermUnit("添加用户", "/template", "/template", false, null));
        adminMap.put("updateUser", new PermUnit("修改用户", "/template", "/template", false, null));
        adminMap.put("deleteUser", new PermUnit("添加用户", "/template", "/template", false, null));
        adminMap.put("addDept", new PermUnit("添加部门", "/template", "/template", false, null));
        adminMap.put("updateDept", new PermUnit("修改部门", "/template", "/template", false, null));
        adminMap.put("deleteDept", new PermUnit("删除部门", "/template", "/template", false, null));
        adminMap.put("addRole", new PermUnit("添加角色", "/template", "/template", false, null));
        adminMap.put("updateRole", new PermUnit("修改角色", "/template", "/template", false, null));
        adminMap.put("deleteRole", new PermUnit("删除角色", "/template", "/template", false, null));


        children.put("template", new PermUnit("模板库", "/user", "/user", false, templateMap));
        children.put("project", new PermUnit("项目管理", "/", "/management", false, projectMap));
        children.put("admin", new PermUnit("后台管理", "/", "/management", false, adminMap));
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
