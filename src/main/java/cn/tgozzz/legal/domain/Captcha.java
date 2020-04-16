package cn.tgozzz.legal.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Captcha implements Serializable {

    private static final long serialVersionUID = 12L;

    private String phone;

    private String code;
}
