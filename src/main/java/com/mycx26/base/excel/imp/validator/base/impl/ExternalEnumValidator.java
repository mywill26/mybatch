package com.mycx26.base.excel.imp.validator.base.impl;

import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.imp.validator.base.ValueValidator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by mycx26 on 2020/9/23.
 */
@Component("e-enumValidator")
public class ExternalEnumValidator extends ValueValidator {

    @Override
    public void validate(List<String> values, Template template, StringBuilder error) {

    }
}
