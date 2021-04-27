package com.mycx26.base.excel.exp.enump;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Export data source:
 * 1. database, origin database without processing
 * 2. method, existed method output export data
 *
 * Created by mycx26 on 2020/6/23.
 */
@Getter
@AllArgsConstructor
public enum ExportSource {

    DB("db"),
    METHOD("method");

    private String code;
}
