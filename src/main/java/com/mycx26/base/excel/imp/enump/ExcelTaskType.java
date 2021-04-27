package com.mycx26.base.excel.imp.enump;

/**
 * Created by mycx26 on 2018/8/18.
 */
public enum ExcelTaskType {

    IMPORT("import"),
    EXPORT("export");

    private String code;

    ExcelTaskType(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
