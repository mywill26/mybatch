package com.mycx26.base.excel.web.controller;


import com.mycx26.base.excel.entity.ExcelTask;
import com.mycx26.base.excel.service.ExcelTaskService;
import com.mycx26.base.excel.service.query.ExcelTaskQuery;
import com.mycx26.base.service.dto.Message;
import com.mycx26.base.service.dto.PageData;
import com.mycx26.base.service.file.CloudFileService;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * <p>
 * excel任务 前端控制器
 * </p>
 *
 * @author mycx26
 * @since 2020-08-04
 */
@RestController
@RequestMapping("ie/task")
public class ExcelTaskController {

    @Resource
    private ExcelTaskService excelTaskService;

    @Lazy
    @Resource
    private CloudFileService cloudFileService;

    @RequestMapping("getFile")
    public void getFile(Long id,
                        HttpServletResponse response) throws IOException {
        ExcelTask excelTask = excelTaskService.getById(id);
        byte[] bytes = cloudFileService.download(excelTask.getExpFilePath());

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(excelTask.getExpFileName(), "UTF-8");
        response.setHeader("Content-disposition",
                "attachment;filename=" + fileName);

        response.setHeader("Access-Control-Expose-Headers", "Content-disposition");

        response.getOutputStream().write(bytes);
    }

    @RequestMapping("getList")
    public Message<PageData<ExcelTask>> getList(ExcelTaskQuery excelTaskQuery) {
        return Message.success(excelTaskService.getList(excelTaskQuery));
    }

    @RequestMapping("getById")
    public Message<ExcelTask> getById(Long id) {
        return Message.success(excelTaskService.getById(id));
    }
}
