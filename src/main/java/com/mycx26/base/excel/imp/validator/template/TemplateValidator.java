package com.mycx26.base.excel.imp.validator.template;

import com.mycx26.base.excel.imp.bo.ImportParam;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Special template biz validator interface, all implementations should be registered in template.
 * <p>
 * Created by mycx26 on 2019/1/11.
 */
public abstract class TemplateValidator implements Serializable {

    public static final String TMPL_VALIDATOR = "TmplValidator";

    private static final long serialVersionUID = 1L;

    public abstract void validate(List<String> values, ImportParam importParam, StringBuilder error);

    public Set<String> getUniqueData(List<String> keys) {
        return Collections.emptySet();
    }

    /**
     * Validate template business parameters not in excel.
     *
     * @param params business parameters
     */
    public void validateParam(Map<String, Object> params) {
    }
}
