package com.mycx26.base.excel.web.controller;


import com.alibaba.excel.EasyExcel;
import com.mycx26.base.excel.constant.ExcelConst;
import com.mycx26.base.excel.entity.Template;
import com.mycx26.base.excel.entity.TemplateCol;
import com.mycx26.base.excel.service.GeneralExcelWriter;
import com.mycx26.base.excel.service.TemplateService;
import com.mycx26.base.service.dto.Message;
import com.mycx26.base.util.ServletUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * excel模板 前端控制器
 * </p>
 *
 * @author mycx26
 * @since 2020-08-04
 */
@RestController
@RequestMapping("ie/tmpl")
public class TemplateController {

    @Resource
    private TemplateService templateService;

    @RequestMapping("getFile")
    public void getTmplFile(String tmplCode,
                           HttpServletResponse response) throws IOException {
        Template template = templateService.getByCode(tmplCode);
        if (null == template) {
            ServletUtil.renderString(response, "Template not exist");
        } else {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode(template.getFileName(), "UTF-8");
            response.setHeader("Content-disposition",
                    "attachment;filename=" + fileName + ExcelConst.EXCEL_2007_SUFFIX);

            response.setHeader("Access-Control-Expose-Headers", "Content-disposition");

            List<String> row = template.getCols().stream()
                    .map(TemplateCol::getColLabel).collect(Collectors.toList());

            EasyExcel.write(response.getOutputStream())
                    .head(GeneralExcelWriter.initHeads(row))
                    .registerWriteHandler(GeneralExcelWriter.getStrategy())
                    .sheet(GeneralExcelWriter.SHEET_NAME).doWrite(Collections.emptyList());
        }
    }

    @RequestMapping("getOneByCode")
    public Message<Template> getOneByCode(String tmplCode) {
        return Message.success(templateService.getOneByCode(tmplCode));
    }
}
