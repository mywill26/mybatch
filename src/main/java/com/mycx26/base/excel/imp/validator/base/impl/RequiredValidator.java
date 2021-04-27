package com.mycx26.base.excel.imp.validator.base.impl;

import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.imp.util.ExcelMsgUtil;
import com.mycx26.base.excel.imp.validator.base.ValueValidator;
import com.mycx26.base.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by mycx26 on 2018/8/19.
 */
@Component
public class RequiredValidator extends ValueValidator {

    @Override
    public void validate(List<String> values, Template template, StringBuilder error) {
        List<Integer> orders = template.getRequiredOrders();
        String value;

        for (Integer order : orders) {
            value = values.get(order - 1);
            if (StringUtil.isBlank(value)) {
                value = null == value ? StringUtil.EMPTY : value;

                error.append(ExcelMsgUtil.appendColMsg(order - 1, value)).append("必填").append(StringUtil.LF);
            }
        }
    }
}
