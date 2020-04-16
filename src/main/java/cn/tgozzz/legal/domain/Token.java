package cn.tgozzz.legal.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Token implements Serializable {

    private static final long serialVersionUID = 11L;

    private String uid;

    private String code;
}
