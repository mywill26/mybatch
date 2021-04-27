package com.mycx26.base.excel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mycx26.base.enump.Yn;
import com.mycx26.base.excel.entity.TemplateCol;
import com.mycx26.base.excel.mapper.TemplateColMapper;
import com.mycx26.base.excel.service.TemplateColService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * excel模板列 服务实现类
 * </p>
 *
 * @author mycx26
 * @since 2020-08-04
 */
@Service
public class TemplateColServiceImpl extends ServiceImpl<TemplateColMapper, TemplateCol> implements TemplateColService {

    @Override
    public List<TemplateCol> getByTmplCode(String tmplCode) {
        return list(new QueryWrapper<TemplateCol>().eq("tmpl_code", tmplCode).orderByAsc("order_no").eq("yn", Yn.YES.getCode()));
    }
}
