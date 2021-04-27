package com.mycx26.base.excel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.service.ExternalEnumService;

/**
 * Created by mycx26 on 2018/8/17.
 */
public interface TemplateService extends IService<Template>, ExternalEnumService {

    Template getByCode(String tmplCode);

    void deleteCacheByCode(String tmplCode);

    // only get template base info
    Template getOneByCode(String tmplCode);
}
