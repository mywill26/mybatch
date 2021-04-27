package com.mycx26.base.excel.imp.validator.base.impl;

import com.mycx26.base.constant.DateConstant;
import com.mycx26.base.enump.ColTypeEnum;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.imp.util.ExcelMsgUtil;
import com.mycx26.base.excel.imp.validator.base.ValueValidator;
import com.mycx26.base.util.StringUtil;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by mycx26 on 2018/8/20.
 */
@Component
public class DateValidator extends ValueValidator {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateConstant.DATE);

    @Override
    public void validate(List<String> values, Template template, StringBuilder error) {
        List<Integer> orders = template.getColTypeOrdersMap().get(ColTypeEnum.DATE.getCode());
        String value;

        for (Integer order : orders) {
            value = values.get(order - 1);
            if (StringUtil.isBlank(value)) {
                continue;
            }

            if (!isDate(value)) {
                error.append(ExcelMsgUtil.appendColMsg(order - 1, value))
                        .append("日期格式不正确，正确格式为【yyyy-MM-dd】").append(StringUtil.LF);
            }
        }
    }

    private boolean isDate(String value) {
        try {
            formatter.parse(value);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
