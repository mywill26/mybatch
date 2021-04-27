package com.mycx26.base.excel.imp.thread;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mycx26.base.excel.constant.ExcelConst;
import com.mycx26.base.excel.entity.ExcelTask;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.imp.bo.ImportParam;
import com.mycx26.base.excel.imp.enump.ExcelTaskStatus;
import com.mycx26.base.excel.imp.enump.ExcelTaskType;
import com.mycx26.base.excel.property.BatchProperty;
import com.mycx26.base.excel.service.ExcelTaskService;
import com.mycx26.base.excel.service.TemplateService;
import com.mycx26.base.excel.service.impl.ExcelTaskServiceImpl;
import com.mycx26.base.exception.ParamException;
import com.mycx26.base.service.file.CloudFileService;
import com.mycx26.base.util.JacksonUtil;
import com.mycx26.base.util.SpringUtil;
import com.mycx26.base.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main thread of scheduling all the tasks.
 *
 * Created by mycx26 on 2019/10/30.
 */
@Service
public class ImportMainThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportMainThreadService.class);

    @Resource
    private CloudFileService cloudFileService;

    @Resource
    private ExcelTaskService excelTaskService;

    @Resource
    private AsyncTaskExecutor taskExecutor;

    @Resource
    private TemplateService templateService;

    @Resource
    private BatchProperty batchProperty;

    @Transactional(rollbackFor = Exception.class)
    public void startImp(MultipartFile file, String tmplCode, String userId, Map<String, Object> params) {
        startImpValidate(file, tmplCode, userId);

        ExcelTask task = initTask(tmplCode, userId, file, params);      // 1st, db task

        bakFile(file, task);    // 2nd, backup original file

        ImportMainThread importMainThread = new ImportMainThread();   // 3rd, start !!!
        taskExecutor.submit(importMainThread);
    }

    private void startImpValidate(MultipartFile file, String tmplCode, String userId) {
        StringBuilder sb = new StringBuilder();

        if (null == file || file.isEmpty()) {
            StringUtil.append(sb, "File is required");
        } else if (null == file.getOriginalFilename() ||
                !file.getOriginalFilename().endsWith(ExcelConst.EXCEL_2007_SUFFIX)) {
            StringUtil.append(sb, "File must be xlsx");
        }

        if (StringUtil.isBlank(tmplCode)) {
            StringUtil.append(sb, "Template code is required");
        } else {
            Template template = templateService.getByCode(tmplCode);
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

    public class ImportMainThread implements Runnable {

        private ExcelTaskService excelTaskService = SpringUtil.getBean(ExcelTaskServiceImpl.class);

        private ImportHandleThreadService importHandleThreadService = SpringUtil.getBean(ImportHandleThreadService.class);

        private ThreadPoolTaskExecutor taskExecutor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);

        @Override
        public void run() {
            LOGGER.info("=============> ImportMainThread start <=============");

            int count = excelTaskService.getImpRunningCount();
            if (count > batchProperty.getImpMaxCount()) {    // todo config count
                return;
            }

            ExcelTask headTask = excelTaskService.getImpHead();
            if (null == headTask) {
                return;
            }

            boolean flag = excelTaskService.update(Wrappers.<ExcelTask>lambdaUpdate()
                    .set(ExcelTask::getTaskStatusCode, ExcelTaskStatus.RUNNING.getCode())
                    .set(ExcelTask::getStartTime, LocalDateTime.now())
                    .eq(ExcelTask::getId, headTask.getId())
                    .eq(ExcelTask::getTaskStatusCode, headTask.getTaskStatusCode())
            );
            if (!flag) {
                return;
            }

            ImportParam importParam = new ImportParam();
            importParam.setCfs(new ArrayList<>(3));
            importParam.setBatchCount(batchProperty.getImpBatchCount());
            ImportHandleThreadService.ImportHandleThread importHandleThread
                    = importHandleThreadService.new ImportHandleThread(importParam, headTask);
            importParam.getCfs().add(CompletableFuture.supplyAsync(importHandleThread, taskExecutor));

            LOGGER.info("=============> ImportMainThread end <=============");
        }
    }
}
