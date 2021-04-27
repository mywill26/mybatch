package com.mycx26.base.excel.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mycx26.base.constant.SqlConstant;
import com.mycx26.base.context.UserAccessContext;
import com.mycx26.base.context.UserContext;
import com.mycx26.base.excel.constant.EnumTypeConst;
import com.mycx26.base.excel.entity.ExcelTask;
import com.mycx26.base.excel.imp.enump.ExcelTaskStatus;
import com.mycx26.base.excel.imp.enump.ExcelTaskType;
import com.mycx26.base.excel.mapper.ExcelTaskMapper;
import com.mycx26.base.excel.service.ExcelTaskService;
import com.mycx26.base.excel.service.TemplateService;
import com.mycx26.base.excel.service.query.ExcelTaskQuery;
import com.mycx26.base.service.EnumValueService;
import com.mycx26.base.service.dto.PageData;
import com.mycx26.base.service.file.CloudFileService;
import com.mycx26.base.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * excel任务 服务实现类
 * </p>
 *
 * @author mycx26
 * @since 2020-08-04
 */
@Service
public class ExcelTaskServiceImpl extends ServiceImpl<ExcelTaskMapper, ExcelTask> implements ExcelTaskService {

    @Resource
    private EnumValueService enumValueService;

    @Resource
    private TemplateService templateService;

    @Resource
    private CloudFileService cloudFileService;

    @Override
    public PageData<ExcelTask> getList(ExcelTaskQuery excelTaskQuery) {
        IPage<ExcelTask> page = page(new Page<>(excelTaskQuery.getCurrent(), excelTaskQuery.getSize()), Wrappers.<ExcelTask>lambdaQuery()
                .eq(!UserAccessContext.isAdmin(), ExcelTask::getUserId, UserContext.getUserId())
                .eq(StringUtil.isNotBlank(excelTaskQuery.getTaskStatusCode()), ExcelTask::getTaskStatusCode, excelTaskQuery.getTaskStatusCode())
                .eq(StringUtil.isNotBlank(excelTaskQuery.getUserId()), ExcelTask::getUserId, excelTaskQuery.getUserId())
                .orderByDesc(ExcelTask::getId));
        if (page.getRecords().isEmpty()) {
            return new PageData<>();
        }

        page.getRecords().forEach(e -> {
            translate(e);
            if (StringUtil.isNotBlank(e.getExpFilePath())) {
                e.setExpFilePath(cloudFileService.getDownloadUrl(e.getExpFilePath(), 3600));
            }
        });
        return new PageData<>(page);
    }

    private void translate(ExcelTask task) {
        task.setTaskType(enumValueService.getNameByTypeAndValueCode(EnumTypeConst.EXCEL_TASK_TYPE, task.getTaskTypeCode()));
        task.setTmplType(templateService.getNameByCode(task.getTmplTypeCode()));
        task.setTaskStatus(enumValueService.getNameByTypeAndValueCode(EnumTypeConst.EXCEL_TASK_STATUS, task.getTaskStatusCode()));
    }

    @Override
    public int getImpRunningCount() {
        return count(Wrappers.<ExcelTask>lambdaQuery()
                .eq(ExcelTask::getTaskTypeCode, ExcelTaskType.IMPORT.getCode())
                .eq(ExcelTask::getTaskStatusCode, ExcelTaskStatus.RUNNING.getCode()));
    }

    @Override
    public ExcelTask getImpHead() {
        return getOne(Wrappers.<ExcelTask>lambdaQuery()
                .eq(ExcelTask::getTaskTypeCode, ExcelTaskType.IMPORT.getCode())
                .in(ExcelTask::getTaskStatusCode, ExcelTaskStatus.NEW.getCode(), ExcelTaskStatus.BLOCK.getCode())
                .orderByAsc(ExcelTask::getId).last(SqlConstant.LIMIT_1)
        );
    }
}
