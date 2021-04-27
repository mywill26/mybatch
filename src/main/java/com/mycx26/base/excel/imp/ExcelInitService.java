package com.mycx26.base.excel.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Created by caixiang155 on 2018/8/27.
 */
@Service
public class ExcelInitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelInitService.class);

    private File impErrorPath;

    private File expPath;

    @PostConstruct
    private void init() {
        @SuppressWarnings({"ConstantConditions"})
        String projectPath = ClassUtils.getDefaultClassLoader().getResource("").getPath();

        File impPath = new File(projectPath);
        String path = impPath.getAbsolutePath();
        path = path.substring(0, path.indexOf(File.separator) + 1);

        String temp = path + "export/App/imp/error/".replace("/", File.separator);
        impErrorPath = new File(temp);

        if (!impErrorPath.exists()) {
            if (!impErrorPath.mkdirs()) {
                LOGGER.error("Create import error file directory fail");
            }
        }

        temp = path + "export/App/exp/".replace("/", File.separator);
        expPath = new File(temp);

        if (!expPath.exists()) {
            if (!expPath.mkdirs()) {
                LOGGER.error("Create export file directory fail");
            }
        }
    }

    public File getImpErrorPath() {
        return impErrorPath;
    }

    public File getExpPath() {
        return expPath;
    }
}
