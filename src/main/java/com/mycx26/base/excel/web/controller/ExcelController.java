package com.mycx26.base.excel.web.controller;

import com.mycx26.base.context.UserContext;
import com.mycx26.base.excel.exp.constant.ExpConst;
import com.mycx26.base.excel.exp.thread.ExportMainThreadService;
import com.mycx26.base.excel.imp.thread.ImportMainThreadService;
import com.mycx26.base.service.dto.Message;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by mycx26 on 2019/10/29.
 */
@RequestMapping("ie")
@Controller
public class ExcelController {

    @Resource
    private ImportMainThreadService importMainThreadService;

    @Resource
    private ExportMainThreadService exportMainThreadService;

    @PostMapping("imp")
    @ResponseBody
    public Message<?> imp(String tmplCode, MultipartFile file,
                       @RequestParam Map<String, Object> params) {
        importMainThreadService.startImp(file, tmplCode, UserContext.getUserId(), params);

        return Message.success();
    }

    @PostMapping("exp")
    @ResponseBody
    public Message<?> exp(@RequestParam Map<String, Object> params) {
        String tmplCode = (String) params.get(ExpConst.TEMPLATE_CODE);
        params.remove(ExpConst.TEMPLATE_CODE);

        return Message.success(exportMainThreadService.startExp(tmplCode, UserContext.getUserId(), params));
    }
}
