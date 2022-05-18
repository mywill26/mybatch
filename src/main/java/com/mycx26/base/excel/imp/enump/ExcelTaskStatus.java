package com.mycx26.base.excel.imp.enump;

/**
 * Created by mycx26 on 2018/8/16.
 */
public enum ExcelTaskStatus {

    /**
     * 新建
     */
    NEW("new", "新建"),

    /**
     * 运行中
     */
    RUNNING("running", "运行中"),

    /**
     * 阻塞
     */
    BLOCK("block", "阻塞"),

    /**
     * 结束
     */
    FINISH("finish", "结束");

    private String code;

    private String name;

    ExcelTaskStatus(String code, String name){
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
