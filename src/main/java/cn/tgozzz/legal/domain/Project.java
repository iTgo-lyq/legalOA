package cn.tgozzz.legal.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "project")
@Data
public class Project {
    @Id
    private String pid;
    private int status = 1; //状态
    private BaseInfo baseInfo = new BaseInfo(); //基本信息
    private CreateInfo createInfo = new CreateInfo(); // 创建者信息
    private Director director = new Director(); // 负责人信息
    private Drafter drafter = new Drafter(); // 拟稿部门信息
    private ArrayList<HistoryUnit> history = new ArrayList<>(); //历史追踪
    private ArrayList<AuditorUnit> auditor = new ArrayList<>(); //审核部门信息列表
    private ArrayList<ContractUnit> contracts = new ArrayList<>(); // 合同列表

    @Data
    @NoArgsConstructor
    public static class BaseInfo {
        private String name = "";
        private String tag = "";
        private String partner = "";
        private String desc = "";
    }

    @Data
    @NoArgsConstructor
    public static class HistoryUnit {
        private long time = new Date().getTime();
        private String info = "";

        public HistoryUnit(String info) {
            this.info = info;
        }
    }

    @Data
    @NoArgsConstructor
    public static class CreateInfo {
        private String uid = "";
        private String name = "";
        private long time = new Date().getTime();

        public CreateInfo(String uid, String name) {
            this.uid = uid;
            this.name = name;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Director {
        private String uid = "";
        private String name = "";
        private String phone = "";
    }

    @Data
    @NoArgsConstructor
    public static class Drafter {
        private String did = "";
        private String name = "";
        private String phone = "";
        private String responsibility = "";
    }

    @Data
    @NoArgsConstructor
    public static class AuditorUnit {
        private String did = "";
        private int order = -1;
        private String name = "";
        private String phone = "";
        private String responsibility = "";
    }

    @Data
    @NoArgsConstructor
    public static class ContractUnit {
        private String cid = "";
        private Contract.BaseInfo baseInfo = new Contract.BaseInfo();
    }
}