package com.mycx26.base.excel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mycx26.base.excel.entity.TemplateCol;

import java.util.List;

/**
 * <p>
 * excel模板列 服务类
 * </p>
 *
 * @author mycx26
 * @since 2020-08-04
 */
public interface TemplateColService extends IService<TemplateCol> {

    List<TemplateCol> getByTmplCode(String tmplCode);
}
