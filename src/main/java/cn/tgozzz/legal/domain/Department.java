package cn.tgozzz.legal.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "department")
@Data
public class Department {
    @Id
    private String did;
    private String name = "";
    private String leader = ""; //负责人名字
    private String leaderMail = "";
    private String leaderPhone = "";
    private String superior = "0"; //上级部门id 0为顶级无父级
    private int status = 0;
    private int grade = 0;
    private long createTime = new Date().getTime(); //创建时间
    private ArrayList<String> leaderRole = new ArrayList<>(); //负责人角色
    private ArrayList<String> memberRole = new ArrayList<>(); //成员角色
    private ArrayList<String> subordinates = new ArrayList<>(); //下级部门id
    private ArrayList<String> updateInfo = new ArrayList<>(); //部门动态
}
