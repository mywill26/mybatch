package com.mycx26.base.excel.imp.thread;

import com.mycx26.base.excel.constant.ExcelConst;
import com.mycx26.base.excel.imp.bo.ImportParam;
import com.mycx26.base.excel.imp.writedb.TemplateDbWriter;
import com.mycx26.base.util.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

/**
 * Created by mycx26 on 2019/10/31.
 */
public class ImportWriteDbThread implements Supplier<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportWriteDbThread.class);

    private ImportParam importParam;

    private BlockingQueue<List<String>> queue;

    private List<List<String>> rows;

    private TemplateDbWriter templateDbWriter;

    ImportWriteDbThread(ImportParam importParam) {
        this.importParam = importParam;
        queue = new LinkedBlockingQueue<>(importParam.getBatchCount());
        rows = new ArrayList<>(importParam.getBatchCount());
        templateDbWriter = SpringUtil.getBean(importParam.getTemplate().getTmplCode() + TemplateDbWriter.DB_WRITER);
    }

    BlockingQueue<List<String>> getQueue() {
        return queue;
    }

    @Override
    public String get() {
        LOGGER.info(importParam.getTaskId() + "=============> ImportWriteDbThread start <=============");

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
            if (row.isEmpty()) {
                if (!rows.isEmpty()) {
                    templateDbWriter.write(rows, importParam);
                    rows.clear();
                    break;
                }
            }
            rows.add(row);
            if (importParam.getBatchCount() == rows.size()) {
                templateDbWriter.write(rows, importParam);
                rows.clear();
            }
        }

        LOGGER.info(importParam.getTaskId() + "=============> ImportWriteDbThread end <=============");

        return ExcelConst.SUCCESS;
    }
}
