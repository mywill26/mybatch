package com.mycx26.base.excel.imp.thread;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mycx26.base.excel.constant.ExcelConst;
import com.mycx26.base.excel.entity.ExcelTask;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.imp.ExcelInitService;
import com.mycx26.base.excel.imp.bo.ImportParam;
import com.mycx26.base.excel.imp.enump.ExcelTaskStatus;
import com.mycx26.base.excel.imp.enump.WriteDbStrategy;
import com.mycx26.base.excel.imp.handler.ImpLifeHandler;
import com.mycx26.base.excel.imp.validator.template.TemplateValidator;
import com.mycx26.base.excel.property.BatchProperty;
import com.mycx26.base.excel.service.ExcelTaskService;
import com.mycx26.base.excel.service.TemplateService;
import com.mycx26.base.exception.ParamException;
import com.mycx26.base.exception.base.AppException;
import com.mycx26.base.service.file.CloudFileService;
import com.mycx26.base.util.JacksonUtil;
import com.mycx26.base.util.SpringUtil;
import com.mycx26.base.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Core handle thread, coordinator for start post threads.
 * <p>
 * Created by mycx26 on 2019/10/29.
 */
@Slf4j
@Service
class ImportHandleThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportWriteExcelThread.class);

    @Resource
    private ExcelTaskService excelTaskService;

    @Lazy
    @Resource
    private CloudFileService cloudFileService;

    @Resource
    private AsyncTaskExecutor taskExecutor;

    @Resource
    private TemplateService templateService;

    @Resource
    private ExcelInitService excelInitService;

    @Resource
    private BatchProperty batchProperty;

    void run() {
        int count = excelTaskService.getImpRunningCount();
        if (count > batchProperty.getImpMaxCount()) {
            return;
        }
        // get head task in queue
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

        ImportParam importParam = initParam(headTask);
        ImportHandleThreadService.ImportHandleThread importHandleThread = new ImportHandleThread(importParam);
        importParam.getCfs().add(CompletableFuture.supplyAsync(importHandleThread, taskExecutor));
    }

    private ImportParam initParam(ExcelTask task) {
        ImportParam importParam = new ImportParam();
        importParam.setUserId(task.getUserId());
        importParam.setTaskId(task.getId());
        importParam.setTemplate(templateService.getByCode(task.getTmplTypeCode()));
        importParam.setParams(JacksonUtil.parseObject(task.getParams(), new TypeReference<Map<String, Object>>() {
        }));
        importParam.setWritePath(excelInitService.getImpErrorPath().getAbsolutePath());
        importParam.setBatchCount(batchProperty.getImpBatchCount());

        importParam.setCfs(new ArrayList<>(3));

        return importParam;
    }

    private class ImportHandleThread implements Supplier<String> {

        private ImportParam importParam;

        ImportHandleThread(ImportParam importParam) {
            this.importParam = importParam;
        }

        @Override
        public String get() {
            LOGGER.info(importParam.getTaskId() + "=============> ImportHandleThread start <=============");

            ExcelReadListener listener = new ExcelReadListener(importParam);
            ExcelTask task = excelTaskService.getById(importParam.getTaskId());

            try {
                ExcelReaderSheetBuilder builder = EasyExcel.read(cloudFileService.downloadSteam(task.getImpFilePath()), listener)
                        .sheet().headRowNumber(importParam.getTemplate().getStartRow());
                importParam.getCfs().get(0).exceptionally(e -> {
                    expHandle(importParam, e);
                    return null;
                });
                builder.doRead();
                CompletableFuture.allOf(importParam.getCfs().toArray(new CompletableFuture[0]))
                        .thenRun(this::postHandle)
                        .exceptionally(e -> {
                            expHandle(importParam, e);
                            return null;
                        });
            } catch (Exception e) {
                try {
                    if (listener.importWriteExcelThread != null) {
                        listener.importWriteExcelThread.getQueue().put(Collections.emptyList());
                    }
                    if (listener.importWriteDbThread != null) {
                        listener.importWriteDbThread.getQueue().put(Collections.emptyList());
                    }
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }
                throw e;
            }

            LOGGER.info(importParam.getTaskId() + "=============> ImportHandleThread end <=============");

            return ExcelConst.SUCCESS;
        }

        private void expHandle(ImportParam importParam, Throwable e) {
            LOGGER.error(importParam.getTaskId() + "task=============> child thread exp <=============", e);
            if (e.getCause().getCause() instanceof AppException) {
                importParam.setError(true);
                importParam.setException(false);
                importParam.setExpDesc(e.getCause().getCause().getMessage());
            } else {
                importParam.setException(true);
            }

            postHandle();
        }

        private void postHandle() {
            ExcelTask task = new ExcelTask();
            task.setId(importParam.getTaskId());
            task.setException(importParam.isException());
            task.setError(importParam.isError());
            task.setTaskStatusCode(ExcelTaskStatus.FINISH.getCode());
            task.setDescription(importParam.getExpDesc());
            task.setSuccessCount(importParam.getSuccessCount());
            task.setFailureCount(importParam.getFailureCount());
            task.setTotalCount(importParam.getTotalCount());

            excelTaskService.updateById(task);

            // end handle
            ImpLifeHandler handler = SpringUtil.getBean2(
                    importParam.getTemplate().getTmplCode() + ImpLifeHandler.IMP_LIFE_HANDLER);
            if (null == handler) {
                return;
            }
            handler.onEnd(importParam);

            run();
        }
    }

    private class ExcelReadListener extends AnalysisEventListener<Map<Integer, String>> {

        private ImportParam importParam;

        private List<List<String>> rows;

        private ImportWriteExcelThread importWriteExcelThread;

        private ImportWriteDbThread importWriteDbThread;

        ExcelReadListener(ImportParam importParam) {
            this.importParam = importParam;
            rows = new ArrayList<>(importParam.getBatchCount());
        }

        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
            List<String> row = new ArrayList<>(data.values());
            int colSize = importParam.getTemplate().getCols().size();

            if (row.size() > colSize) {
                throw new ParamException("模板错误");
            } else if (row.size() < colSize) {
                for (int i = 0, size = colSize - row.size(); i < size; i++) {
                    row.add(StringUtil.EMPTY);
                }
            }
            rows.add(row);
            importParam.addTotalCount();

            if (importParam.getBatchCount() == rows.size()) {
                doHandle();
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            doHandle();

            try {
                if (importWriteExcelThread != null) {
                    importWriteExcelThread.getQueue().put(Collections.emptyList());
                }
                if (importWriteDbThread != null) {
                    importWriteDbThread.getQueue().put(Collections.emptyList());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void doHandle() {
            if (importParam.isException() || rows.isEmpty()) {
                return;
            }

            Template template = importParam.getTemplate();
            Boolean isUniqueness = template.getIsUniqueness();
            String dbStrategyCode = template.getDbStrategyCode();

            Set<String> dbUniques = Collections.emptySet();
            TemplateValidator tmplValidator = template.getTemplateValidator();
            if (isUniqueness) {
                int uniqueOrderNo = template.getUniqueOrderNo();
                List<String> uniques = rows.stream().map(e -> e.get(uniqueOrderNo - 1)).collect(Collectors.toList());
                dbUniques = tmplValidator.getUniqueData(uniques);
            }

            Set<String> finalDbUniques = dbUniques;
            rows.forEach(row -> {
                StringBuilder error = new StringBuilder();
                // ======================= base validate tier =======================
                template.getValidators().forEach(validator -> validator.validate(row, template, error));
                // ======================= base validate tier =======================

                // ======================= template validate tier =======================
                if (isUniqueness) {
                    String uniqueValue = row.get(template.getUniqueOrderNo() - 1);
                    if (StringUtil.isNotBlank(uniqueValue)) {
                        if (WriteDbStrategy.INSERT.getCode().equals(dbStrategyCode)) {
                            if (finalDbUniques.contains(uniqueValue)) {
                                StringUtil.append(error, "数据已存在");
                            }
                        } else if (WriteDbStrategy.UPDATE.getCode().equals(dbStrategyCode)
                                || WriteDbStrategy.ALL.getCode().equals(dbStrategyCode)) {
                            if (!finalDbUniques.contains(uniqueValue)) {
                                StringUtil.append(error, "数据不存在");
                            }
                        }
                    }
                }
                if (0 == error.length()) {
                    tmplValidator.validate(row, importParam, error);
                }
                // ======================= template validate tier =======================

                if (error.length() > 0) {
                    row.add(error.toString());
                    importParam.setError(true);
                }

                ArrayList<String> list = new ArrayList<>(row.size() + 6);
                //noinspection unchecked,rawtypes
                list.addAll((ArrayList<String>) ((ArrayList) row).clone());

                if (error.length() > 0) {
                    importParam.addFailureCount();
                    if (null == importWriteExcelThread) {
                        importWriteExcelThread = new ImportWriteExcelThread(importParam, cloudFileService);
                        importParam.getCfs().add(CompletableFuture.supplyAsync(importWriteExcelThread, taskExecutor));
                    }
                    try {
                        importWriteExcelThread.getQueue().put(list);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    importParam.addSuccessCount();
                    if (template.getIsWriteDb()) {
                        if (null == importWriteDbThread) {
                            importWriteDbThread = new ImportWriteDbThread(importParam);
                            importParam.getCfs().add(CompletableFuture.supplyAsync(importWriteDbThread, taskExecutor));
                        }
                        try {
                            importWriteDbThread.getQueue().put(list);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            rows.clear();
        }
    }
}
