package com.mycx26.base.excel.imp.enump;

/**
 * Created by mycx26 on 2019-06-13.
 */
public enum WriteDbStrategy {

    INSERT("insert"),
    UPDATE("update"),
    ALL("all");

    private String code;

    WriteDbStrategy(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
