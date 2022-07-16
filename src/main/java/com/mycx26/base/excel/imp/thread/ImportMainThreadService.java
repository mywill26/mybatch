package com.mycx26.base.excel.imp.thread;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mycx26.base.excel.constant.ExcelConst;
import com.mycx26.base.excel.entity.ExcelTask;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.imp.enump.ExcelTaskStatus;
import com.mycx26.base.excel.imp.enump.ExcelTaskType;
import com.mycx26.base.excel.imp.validator.template.TemplateValidator;
import com.mycx26.base.excel.service.ExcelTaskService;
import com.mycx26.base.excel.service.TemplateService;
import com.mycx26.base.exception.ParamException;
import com.mycx26.base.service.file.CloudFileService;
import com.mycx26.base.util.JacksonUtil;
import com.mycx26.base.util.SpringUtil;
import com.mycx26.base.util.StringUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

/**
 * Main thread service of scheduling all the tasks.
 * <p>
 * Created by mycx26 on 2019/10/30.
 */
@Service
public class ImportMainThreadService {

    @Lazy
    @Resource
    private CloudFileService cloudFileService;

    @Resource
    private ExcelTaskService excelTaskService;

    @Resource
    private AsyncTaskExecutor taskExecutor;

    @Resource
    private TemplateService templateService;

    @Resource
    private ImportHandleThreadService importHandleThreadService;

    private ImportMainThreadService importMainThreadService;

    @PostConstruct
    private void init() {
        importMainThreadService = SpringUtil.getBean(ImportMainThreadService.class);
    }


    public void startImp(MultipartFile file, String tmplCode, String userId, Map<String, Object> params) {
        importMainThreadService.doStartImp(file, tmplCode, userId, params);
        // 3rd, run !!!
        taskExecutor.submit(importHandleThreadService::run);
    }

    @Transactional(rollbackFor = Exception.class)
    public void doStartImp(MultipartFile file, String tmplCode, String userId, Map<String, Object> params) {
        startImpValidate(file, tmplCode, userId, params);
        // 1st, db task
        ExcelTask task = initTask(tmplCode, userId, file, params);
        // 2nd, backup original file
        bakFile(file, task);
    }

    private void startImpValidate(MultipartFile file, String tmplCode, String userId, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();

        if (null == file || file.isEmpty()) {
            StringUtil.append(sb, "File is required");
        } else if (null == file.getOriginalFilename() ||
                !file.getOriginalFilename().endsWith(ExcelConst.EXCEL_2007_SUFFIX)) {
            StringUtil.append(sb, "File must be xlsx");
        }

        Template template = null;
        if (StringUtil.isBlank(tmplCode)) {
            StringUtil.append(sb, "Template code is required");
        } else {
            template = templateService.getByCode(tmplCode);
            if (null == template) {
                StringUtil.append(sb, "Template not exist");
            }
        }
        if (StringUtil.isBlank(userId)) {
            StringUtil.append(sb, "User id is required");
        }

        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
            throw new ParamException(sb.toString());
        }

        assert template != null;
        TemplateValidator tmplValidator = template.getTemplateValidator();
        tmplValidator.validateParam(params);
    }

    private ExcelTask initTask(String tmplCode, String userId, MultipartFile file, Map<String, Object> params) {
        ExcelTask task = new ExcelTask();

        task.setTaskTypeCode(ExcelTaskType.IMPORT.getCode())
                .setTmplTypeCode(tmplCode)
                .setImpFileName(file.getOriginalFilename())
                .setUserId(userId)
                .setTaskStatusCode(ExcelTaskStatus.BLOCK.getCode())
                .setParams(JacksonUtil.toJsonString(params));

        if (!excelTaskService.save(task)) {
            throw new RuntimeException("Add task fail");
        }

        return task;
    }

    private void bakFile(MultipartFile file, ExcelTask excelTask) {
        String cloudPath = cloudFileService.upload(file, excelTask.getUserId());

        excelTaskService.update(new UpdateWrapper<ExcelTask>()
                .set("imp_file_path", cloudPath)
                .eq("id", excelTask.getId())
        );
    }
}
