package com.mycx26.base.excel.imp.validator.base.impl;

import com.mycx26.base.entity.EnumType;
import com.mycx26.base.entity.EnumValue;
import com.mycx26.base.enump.ColTypeEnum;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.entity.TemplateCol;
import com.mycx26.base.excel.imp.util.ExcelMsgUtil;
import com.mycx26.base.excel.imp.validator.base.ValueValidator;
import com.mycx26.base.service.EnumTypeService;
import com.mycx26.base.service.EnumValueService;
import com.mycx26.base.util.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by mycx26 on 2018/8/19.
 */
@Component
public class EnumValidator extends ValueValidator {

    @Resource
    private EnumValueService enumValueService;

    @Resource
    private EnumTypeService enumTypeService;

    @Override
    public void validate(List<String> values, Template template, StringBuilder error) {
        List<Integer> orders = template.getColTypeOrdersMap().get(ColTypeEnum.ENUM.getCode());
        String value;
        List<TemplateCol> cols = template.getCols();
        TemplateCol col;

        for (Integer order : orders) {
            value = values.get(order - 1);
            if (StringUtil.isBlank(value)) {
                continue;
            }

            col = cols.get(order - 1);
            validateEnum(value, col, values, cols, error);
        }
    }

    private void validateEnum(String value,
                              TemplateCol col,
                              List<String> values,
                              List<TemplateCol> cols,
                              StringBuilder error) {
        EnumType et = enumTypeService.getByTypeCode(col.getEnumTypeCode());

        // cascade enum, from the beginning handle
        if (et.getCascadeEnum() && StringUtil.isNotBlank(et.getParentCode())) {
            return;
        }

        EnumValue ev = enumValueService.getRootByTypeCodeAndValueName(col.getEnumTypeCode(), value);
        if (null == ev) {
            error.append(ExcelMsgUtil.appendColMsg(col.getOrderNo() - 1, value)).append("不是枚举").append(StringUtil.LF);
            return;
        }

        // cascade root enum
        if (et.getCascadeEnum() && StringUtil.isBlank(et.getParentCode())) {
            cascadeValidate(ev, col, values, cols, error);
        }
    }

    private void cascadeValidate(EnumValue ev,
                                 TemplateCol col,
                                 List<String> values,
                                 List<TemplateCol> cols,
                                 StringBuilder error) {
        if (null == col.getChildOrderNo()) {
            return;
        }

        String cValue = values.get(col.getChildOrderNo() - 1);
        TemplateCol cCol = cols.get(col.getChildOrderNo() - 1);

        if (!ev.getNameEvMap().containsKey(cValue)) {
            error.append(ExcelMsgUtil.appendColMsg(col.getOrderNo() - 1, ev.getValueName()))
                    .append("关联的")
                    .append(ExcelMsgUtil.appendColMsg(cCol.getOrderNo() - 1, cValue))
                    .append("没有级联关系").append(StringUtil.LF);
        } else {
            String cValueCode = ev.getNameEvMap().get(cValue).getValueCode();
            ev = enumValueService.getByTypeAndValueCode(cCol.getEnumTypeCode(), cValueCode);

            cascadeValidate(ev, cCol, values, cols, error);
        }
    }
}
