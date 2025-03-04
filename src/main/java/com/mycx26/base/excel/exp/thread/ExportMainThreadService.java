package com.mycx26.base.excel.exp.thread;

import com.mycx26.base.excel.entity.ExcelTask;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.entity.TemplateCol;
import com.mycx26.base.excel.exp.bo.ExportParam;
import com.mycx26.base.excel.exp.enump.ExportSource;
import com.mycx26.base.excel.exp.handler.ExpLifeHandler;
import com.mycx26.base.excel.exp.readdb.ExportSourceReader;
import com.mycx26.base.excel.exp.readdb.impl.MethodReader;
import com.mycx26.base.excel.exp.readdb.impl.SqlDbReader;
import com.mycx26.base.excel.imp.ExcelInitService;
import com.mycx26.base.excel.imp.enump.ExcelTaskStatus;
import com.mycx26.base.excel.imp.enump.ExcelTaskType;
import com.mycx26.base.excel.property.BatchProperty;
import com.mycx26.base.excel.service.ExcelTaskService;
import com.mycx26.base.excel.service.GeneralExcelWriter;
import com.mycx26.base.excel.service.TemplateService;
import com.mycx26.base.excel.util.ExcelUtil;
import com.mycx26.base.exception.base.AppException;
import com.mycx26.base.service.file.CloudFileService;
import com.mycx26.base.util.ObjectUtil;
import com.mycx26.base.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by mycx26 on 2019/11/3.
 */
@Service
public class ExportMainThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportMainThreadService.class);

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
    private ExcelInitService excelInitService;

    @Resource
    private SqlDbReader sqlDbReader;

    @Resource
    private MethodReader methodReader;

    @Resource
    private BatchProperty batchProperty;

    public Long startExp(String tmplCode, String userId, Map<String, Object> params) {
        Template template = templateService.getByCode(tmplCode);
        if (null == template) {
            throw new RuntimeException("Template not exist");
        }

        ExcelTask task = initTask(tmplCode, userId);
        ExportMainThread exportMainThread = new ExportMainThread(params, task, template);

        taskExecutor.execute(exportMainThread);

        return task.getId();
    }

    private ExcelTask initTask(String tmplCode, String userId) {
        ExcelTask task = new ExcelTask();
        task.setTaskTypeCode(ExcelTaskType.EXPORT.getCode());
        task.setTmplTypeCode(tmplCode);
        task.setUserId(userId);
        task.setTaskStatusCode(ExcelTaskStatus.RUNNING.getCode());
        task.setStartTime(LocalDateTime.now());

        if (!excelTaskService.save(task)) {
            throw new RuntimeException("Add task fail");
        }
        return task;
    }

    public class ExportMainThread implements Runnable {

        private ExportParam exportParam = new ExportParam();

        private ExportSourceReader exportReader;

        private GeneralExcelWriter generalExcelWriter;

        private String filePath;

        ExportMainThread(Map<String, Object> params, ExcelTask task, Template template) {
            exportParam.setUserId(task.getUserId());
            exportParam.setTaskId(task.getId());
            exportParam.setTemplate(template);
            exportParam.setWritePath(excelInitService.getExpPath().getAbsolutePath());
            params.forEach((k, v) -> {
                if (ObjectUtil.isEmpty(v)) {
                    params.put(k, null);
                }
            });
            exportParam.setParams(params);
            exportParam.setBatchCount(batchProperty.getExpBatchCount());

            if (ExportSource.DB.getCode().equals(template.getSourceCode())) {
                exportReader = sqlDbReader;
            } else {
                exportReader = methodReader;
            }

            String fileName = ExcelUtil.rename(template.getFileName());
            filePath = exportParam.getWritePath() + File.separator + fileName;
            List<String> labels = template.getCols().stream().map(TemplateCol::getColLabel).collect(Collectors.toList());

            generalExcelWriter = new GeneralExcelWriter(filePath, labels);
        }

        @Override
        public void run() {
            LOGGER.info("=============> ExportMainThread start: {} <=============", exportParam.getTaskId());

            int count = 1;
            try {
                for (; ; ) {
                    List<List<String>> list = exportReader.read(count, exportParam);
                    if (list.isEmpty() || list.size() < exportParam.getBatchCount()) {
                        list.add(Collections.emptyList());
                        generalExcelWriter.writeRows(list);
                        break;
                    }
                    generalExcelWriter.writeRows(list);
                    count++;
                }
                overHandle();
            } catch (AppException e) {
                exportParam.setError(true);
                exportParam.setExpDesc(e.getMessage());
            } catch (Exception e) {
                LOGGER.error("Export main thread exp: ", e);
                exportParam.setException(true);
            }

            ExcelTask task = new ExcelTask();
            task.setId(exportParam.getTaskId())
                    .setError(exportParam.isError())
                    .setException(exportParam.isException())
                    .setTaskStatusCode(ExcelTaskStatus.FINISH.getCode())
                    .setDescription(exportParam.getExpDesc());

            excelTaskService.updateById(task);
            lifeHandle(exportParam);

            LOGGER.info("=============> ExportMainThread end: {} <=============", exportParam.getTaskId());
        }

        private void overHandle() {
            File file = new File(filePath);
            String cloudPath = cloudFileService.upload(file, exportParam.getUserId());

            ExcelTask task = new ExcelTask();
            task.setId(exportParam.getTaskId());
            task.setExpFileName(file.getName());
            task.setExpFilePath(cloudPath);

            excelTaskService.updateById(task);

            if (!file.delete()) {
                LOGGER.error("File delete fail: [{}]", file.getAbsolutePath());
            }
        }

        private void lifeHandle(ExportParam exportParam) {
            ExpLifeHandler handler = SpringUtil.getBean2(
                    exportParam.getTemplate().getTmplCode() + ExpLifeHandler.EXP_LIFE_HANDLER);
            if (null == handler) {
                return;
            }

            if (exportParam.isError() || exportParam.isException()) {
                handler.onFailure(exportParam);
                return;
            }

            handler.onSuccess(exportParam);
        }
    }
}
