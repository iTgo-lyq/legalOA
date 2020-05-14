package cn.tgozzz.legal.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contract")
@Data
public class Contract {
    @Id
    private String cid;

    @Data
    @NoArgsConstructor
    public static class BaseInfo {
        private String name;
    }
}
