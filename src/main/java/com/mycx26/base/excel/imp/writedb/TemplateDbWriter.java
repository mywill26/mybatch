package com.mycx26.base.excel.imp.writedb;

import com.mycx26.base.excel.imp.bo.ImportParam;

import java.util.List;

/**
 * Created by mycx26 on 2019/11/1.
 */
@FunctionalInterface
public interface TemplateDbWriter {

    String DB_WRITER = "DbWriter";

    void write(List<List<String>> rows, ImportParam importParam);
}
