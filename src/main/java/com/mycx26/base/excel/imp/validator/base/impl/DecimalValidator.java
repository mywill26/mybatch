package com.mycx26.base.excel.imp.validator.base.impl;

import com.mycx26.base.enump.ColTypeEnum;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.imp.util.ExcelMsgUtil;
import com.mycx26.base.excel.imp.validator.base.ValueValidator;
import com.mycx26.base.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by mycx26 on 2018/8/19.
 */
@Component
public class DecimalValidator extends ValueValidator {

    private Pattern pattern = Pattern.compile("^(-?\\d+)(.\\d+)?$");

    @Override
    public void validate(List<String> values, Template template, StringBuilder error) {
        List<Integer> orders = template.getColTypeOrdersMap().get(ColTypeEnum.DECIMAL.getCode());
        String value;

        for (Integer order : orders) {
            value = values.get(order - 1);
            if (StringUtil.isBlank(value)) {
                continue;
            }

            if (!pattern.matcher(value).matches()) {
                error.append(ExcelMsgUtil.appendColMsg(order - 1, value)).append("不是小数").append(StringUtil.LF);
            }
        }
    }
}
