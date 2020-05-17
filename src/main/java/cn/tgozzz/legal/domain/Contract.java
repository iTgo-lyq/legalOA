package cn.tgozzz.legal.domain;

import cn.tgozzz.legal.handler.ContractHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "contract")
@Data
public class Contract {
    public static int EDIT_STATUS = 0;
    public static int AUDIT_STATUS = 1;
    public static int COMPLETE_STATUS = 2;
    public static int TRASH_STATUS = -1;

    public static String BASE_URI = "http://legal.tgozzz.cn/office/files/__ffff_127.0.0.1/";

    @Id
    private String cid;
    private String uri = "";
    private int status = 0;
    private String handler = "";
    private CreateInfo createInfo = new CreateInfo();
    private BaseInfo baseInfo = new BaseInfo();
    private ArrayList<History> histories = new ArrayList<>();

    public void setCreateInfo(User user) {
        CreateInfo createInfo = this.getCreateInfo();
        createInfo.setName(user.getName());
        createInfo.setUid(user.getUid());
    }

    @Data
    @NoArgsConstructor
    public static class CreateInfo {
        private String uid = "";
        private String name = "";
        private long time = new Date().getTime();
    }

    @Data
    @NoArgsConstructor
    public static class BaseInfo {
        private String name = "";
        private String type = "";
        private String tag = "";
        private String project = "";
        private String desc = "";
    }

    @Data
    @NoArgsConstructor
    public static class History {
        public static int SYSTEM_TYPE = 0;
        public static int EDIT_TYPE = 1;
        public static int AUDIT_TYPE = 2;
        public static int FLOW_TYPE = 3;

        private String cid = "";
        private int type = 0;
        private long time = new Date().getTime();
        private String info = ""; // 梗概
        private String modifierName = "";
        private String modifierUid = "";
        private String modifyAdvice = ""; // 详细修改意见

        public History(String cid, int type, String info) {
            this.cid = cid;
            this.type = type;
            this.info = info;
        }

        public History(String cid, int type, ContractHandler.AddInfoUnit info) {
            this.cid = cid;
            this.type = type;
            this.info = info.getSummary();
            this.modifyAdvice = info.getDetails();
        }

        public History setModifier(User u) {
            this.modifierName = u.getName();
            this.modifierUid = u.getUid();
            return this;
        }
    }
}
