package com.mycx26.base.excel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mycx26.base.enump.ColTypeEnum;
import com.mycx26.base.enump.Yn;
import com.mycx26.base.excel.constant.ExcelCacheConst;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.entity.TemplateCol;
import com.mycx26.base.excel.imp.enump.ExcelTaskType;
import com.mycx26.base.excel.imp.validator.base.ValueValidator;
import com.mycx26.base.excel.imp.validator.base.impl.RequiredValidator;
import com.mycx26.base.excel.imp.validator.template.TemplateValidator;
import com.mycx26.base.excel.mapper.TemplateMapper;
import com.mycx26.base.excel.service.TemplateColService;
import com.mycx26.base.excel.service.TemplateService;
import com.mycx26.base.util.SpringUtil;
import com.mycx26.base.util.StringUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by mycx26 on 2018/8/17.
 * <p>
 * Update validators on 2020/8/14.
 */
@Service
public class TemplateServiceImpl extends ServiceImpl<TemplateMapper, Template> implements TemplateService {

    @Resource
    private TemplateColService templateColService;

    private TemplateService templateService;

    @PostConstruct
    private void init() {
        templateService = SpringUtil.getBean(TemplateServiceImpl.class);
    }

    @Cacheable(value = ExcelCacheConst.IE_TEMPLATE, key = "#tmplCode",
            condition = "#tmplCode != null", unless = "null == #result")
    @Override
    public Template getByCode(String tmplCode) {
        if (StringUtil.isBlank(tmplCode)) {
            return null;
        }
        Template template = getOne(new QueryWrapper<Template>().eq("tmpl_code", tmplCode).eq("yn", Yn.YES.getCode()));
        if (null == template) {
            return null;
        }
        List<TemplateCol> cols = templateColService.getByTmplCode(tmplCode);
        if (cols.isEmpty()) {
            return template;
        }

        template.setCols(cols);
        if (!ExcelTaskType.IMPORT.getCode().equals(template.getCategoryCode())) {
            return template;
        }

        Map<String, List<Integer>> colTypeOrdersMap = template.getColTypeOrdersMap();
        List<Integer> requiredOrders = template.getRequiredOrders();

        List<ValueValidator> validators = template.getValidators();
        validators.add(SpringUtil.getBean(RequiredValidator.class));

        String colType;
        Integer colOrder;
        Set<String> validatorNames = new HashSet<>(6);

        for (TemplateCol col : cols) {
            colType = col.getColTypeCode();
            colOrder = col.getOrderNo();

            if (!colTypeOrdersMap.containsKey(colType)) {    // handle col type indexes
                colTypeOrdersMap.put(colType, new ArrayList<>());
            }
            colTypeOrdersMap.get(colType).add(colOrder);

            if (col.getRequired() != null && col.getRequired()) {      // handle required columns
                requiredOrders.add(colOrder);
            }

            if (!ColTypeEnum.STRING.getCode().equals(colType)) {
                validatorNames.add(colType);
            }

            if (col.getUniqueness() != null && col.getUniqueness()) {
                template.setIsUniqueness(true);
                template.setUniqueOrderNo(col.getOrderNo());
            }

            if (!template.getTblColOrderMap().containsKey(col.getTableName())) {
                Map<String, Integer> colOrderMap = new HashMap<>(cols.size());
                colOrderMap.put(col.getColName(), colOrder);
                template.getTblColOrderMap().put(col.getTableName(), colOrderMap);
            } else {
                template.getTblColOrderMap().get(col.getTableName()).put(col.getColName(), colOrder);
            }

            if (!template.getTblColNames().containsKey(col.getTableName())) {
                List<String> colNames = new ArrayList<>();
                colNames.add(col.getColName());
                template.getTblColNames().put(col.getTableName(), colNames);
            } else {
                template.getTblColNames().get(col.getTableName()).add(col.getColName());
            }

            if (!template.getTblColOrders().containsKey(col.getTableName())) {
                List<Integer> colOrders = new ArrayList<>();
                colOrders.add(col.getOrderNo());
                template.getTblColOrders().put(col.getColName(), colOrders);
            } else {
                template.getTblColOrders().get(col.getTableName()).add(col.getOrderNo());
            }
        }

        validatorNames.forEach(name -> validators.add(SpringUtil.getBean(name + ValueValidator.VALIDATOR)));     // automatic assembly validators according to type
        template.setTemplateValidator(SpringUtil.getBean2(tmplCode + TemplateValidator.TMPL_VALIDATOR));

        return template;
    }

    @CacheEvict(value = ExcelCacheConst.IE_TEMPLATE, key = "#typeCode")
    @Override
    public void deleteCacheByCode(String typeCode) {
    }

    @Override
    public String getNameByCode(String code) {
        Template template = templateService.getByCode(code);
        if (template != null) {
            return template.getTmplName();
        }

        return null;
    }

    @Override
    public Template getOneByCode(String tmplCode) {
        return getOne(Wrappers.<Template>lambdaQuery().eq(Template::getTmplCode, tmplCode).eq(Template::getYn, Yn.YES.getCode()));
    }
}
