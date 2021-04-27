package com.mycx26.base.excel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mycx26.base.excel.entity.ExcelTask;
import com.mycx26.base.excel.service.query.ExcelTaskQuery;
import com.mycx26.base.service.dto.PageData;

/**
 * <p>
 * excel任务 服务类
 * </p>
 *
 * @author mycx26
 * @since 2020-08-04
 */
public interface ExcelTaskService extends IService<ExcelTask> {

    PageData<ExcelTask> getList(ExcelTaskQuery excelTaskQuery);

    // get count of running tasks
    int getImpRunningCount();

    // get head task of import waiting queue
    ExcelTask getImpHead();
}
