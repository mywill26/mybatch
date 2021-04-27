package com.mycx26.base.excel.imp;

import com.mycx26.base.excel.entity.Template;

import java.util.List;

/**
 * Created by mycx26 on 2020/11/6.
 */
public interface TemplateMethod {

    default String getValueByTblAndCol(String tblName, String colName, List<String> values, Template template) {
        Integer order = getOrderByTblAndCol(tblName, colName, template);
        if (null == order) {
            return null;
        }

        return values.get(order - 1);
    }

    default Integer getOrderByTblAndCol(String tblName, String colName, Template template) {
        return template.getTblColOrderMap().get(tblName).get(colName);
    }
}
