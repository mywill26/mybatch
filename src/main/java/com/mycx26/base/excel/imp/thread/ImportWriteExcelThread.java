package com.mycx26.base.excel.imp.thread;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mycx26.base.excel.constant.ExcelConst;
import com.mycx26.base.excel.entity.ExcelTask;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.entity.TemplateCol;
import com.mycx26.base.excel.imp.bo.ImportParam;
import com.mycx26.base.excel.service.ExcelTaskService;
import com.mycx26.base.excel.service.GeneralExcelWriter;
import com.mycx26.base.excel.service.impl.ExcelTaskServiceImpl;
import com.mycx26.base.excel.util.ExcelUtil;
import com.mycx26.base.service.file.CloudFileService;
import com.mycx26.base.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by mycx26 on 2019/10/31.
 */
public class ImportWriteExcelThread implements Supplier<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportWriteExcelThread.class);

    private BlockingQueue<List<String>> queue;

    private ImportParam importParam;

    private GeneralExcelWriter generalExcelWriter;

    private List<List<String>> rows;

    private ExcelTaskService excelTaskService;

    private String filePath;

    private CloudFileService cloudFileService;

    ImportWriteExcelThread(ImportParam importParam, CloudFileService cloudFileService) {
        queue = new LinkedBlockingQueue<>(importParam.getBatchCount());
        this.importParam = importParam;
        initExcelWriter(importParam);
        rows = new ArrayList<>(importParam.getBatchCount());

        excelTaskService = SpringUtil.getBean(ExcelTaskServiceImpl.class);
        this.cloudFileService = cloudFileService;
    }

    BlockingQueue<List<String>> getQueue() {
        return queue;
    }

    private void initExcelWriter(ImportParam importParam) {
        Template template = importParam.getTemplate();
        String fileName = ExcelUtil.rename(template.getFileName());

        filePath = importParam.getWritePath() + File.separator + fileName;
        List<String> labels = template.getCols().stream().map(TemplateCol::getColLabel).collect(Collectors.toList());

        generalExcelWriter = new GeneralExcelWriter(filePath, labels);
    }

    @Override
    public String get() {
        LOGGER.info(importParam.getTaskId() + "=============> ImportWriteExcelThread start <=============");

        for (; ; ) {
            if (importParam.isException()) {
                break;
            }
            List<String> row;
            try {
                row = queue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            rows.add(row);
            if (row.isEmpty()) {
                generalExcelWriter.writeRows(rows);
                overHandle();
                break;
            }
            if (importParam.getBatchCount() == rows.size()) {
                generalExcelWriter.writeRows(rows);
                rows.clear();
            }
        }

        LOGGER.info(importParam.getTaskId() + "=============> ImportWriteExcelThread end <=============");

        return ExcelConst.SUCCESS;
    }

    private void overHandle() {
        File file = new File(filePath);
        String cloudPath = cloudFileService.upload(file, importParam.getUserId());

        excelTaskService.update(new UpdateWrapper<ExcelTask>()
                .set("exp_file_name", file.getName())
                .set("exp_file_path", cloudPath)
                .eq("id", importParam.getTaskId())
        );

        if (!file.delete()) {
            LOGGER.error("[{}] file delete fail", file.getName());
        }
    }
}
