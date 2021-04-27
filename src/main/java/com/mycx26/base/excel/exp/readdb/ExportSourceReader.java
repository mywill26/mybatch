package com.mycx26.base.excel.exp.readdb;

import com.mycx26.base.constant.Symbol;
import com.mycx26.base.enump.ColTypeEnum;
import com.mycx26.base.excel.entity.TemplateCol;
import com.mycx26.base.excel.exp.bo.ExportParam;
import com.mycx26.base.service.EnumValueService;
import com.mycx26.base.service.ExternalEnumService;
import com.mycx26.base.util.SpringUtil;
import com.mycx26.base.util.StringUtil;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mycx26 on 2019/11/1.
 */
public abstract class ExportSourceReader {

    @Resource
    private EnumValueService enumValueService;

    private Map<String, ExternalEnumService> eEnumMap = new HashMap<>();

    public abstract List<List<String>> read(int current, ExportParam exportParam);

    protected List<List<String>> postHandle(List<Map<String, Object>> rowList, List<TemplateCol> cols) {
        List<List<String>> newRowList = new ArrayList<>(rowList.size());

        rowList.forEach(row -> {
            List<String> newRow = new ArrayList<>(row.size());

            cols.forEach(col -> {
                String value = StringUtil.valueOf(row.get(handleColName(col.getColName())));

                if (StringUtil.isBlank(value)) {
                    newRow.add(StringUtil.nullToEmpty(value));
                } else {
                    if (ColTypeEnum.ENUM.getCode().equals(col.getColTypeCode())) {
                        newRow.add(enumValueService.getNameByTypeAndValueCode(col.getEnumTypeCode(), value));
                    } else if (ColTypeEnum.EXTERNAL_ENUM.getCode().equals(col.getColTypeCode())) {
                        if (null == eEnumMap.get(col.getEnumTypeCode())) {
                            eEnumMap.put(col.getEnumTypeCode(), SpringUtil.getBean(col.getEnumTypeCode()));
                        }
                        ExternalEnumService externalEnumService = eEnumMap.get(col.getEnumTypeCode());
                        newRow.add(externalEnumService.getNameByCode(value));
                    } else {
                        newRow.add(StringUtil.nullToEmpty(value));
                    }
                }
            });

            newRowList.add(newRow);
        });

        return newRowList;
    }

    private String handleColName(String colName) {
        if (colName.contains(Symbol.SPACE)) {
            return colName.substring(colName.lastIndexOf(Symbol.SPACE) + 1);
        }
        return colName;
    }
}
