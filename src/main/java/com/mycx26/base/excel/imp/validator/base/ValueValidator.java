package com.mycx26.base.excel.imp.validator.base;

import com.mycx26.base.excel.entity.Template;

import java.io.Serializable;
import java.util.List;

/**
 * base value validator
 *
 * Created by mycx26 on 2018/8/19.
 */
public abstract class ValueValidator implements Serializable {

    public static final String VALIDATOR = "Validator";

    private static final long serialVersionUID = 1L;

    public abstract void validate(List<String> values, Template template, StringBuilder error);
}
