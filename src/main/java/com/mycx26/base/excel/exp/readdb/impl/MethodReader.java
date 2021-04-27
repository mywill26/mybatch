package com.mycx26.base.excel.exp.readdb.impl;

import com.mycx26.base.constant.Symbol;
import com.mycx26.base.excel.exp.bo.ExportParam;
import com.mycx26.base.excel.exp.readdb.ExportSourceReader;
import com.mycx26.base.exception.base.AppException;
import com.mycx26.base.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Read and handle data by method output.
 *
 * Created by mycx26 on 2020/9/22.
 */
@Slf4j
@Component
public class MethodReader extends ExportSourceReader {

    @SuppressWarnings("unchecked")
    @Override
    public List<List<String>> read(int current, ExportParam exportParam) {
        String sourceKey = exportParam.getTemplate().getSourceKey();
        String clazzName = sourceKey.substring(0, sourceKey.lastIndexOf(Symbol.DOT));
        String methodName = sourceKey.substring(sourceKey.lastIndexOf(Symbol.DOT) + 1);

        List<Map<String, Object>> rowList;
        try {
            Object object = SpringUtil.getBean(Class.forName(clazzName));
            Method method = object.getClass().getDeclaredMethod(methodName, int.class, ExportParam.class);
            method.setAccessible(true);
            rowList = (List<Map<String, Object>>) method.invoke(object, current, exportParam);
        } catch (Exception e) {
            throw new AppException("Method reader read error: ", e);
        }

        return postHandle(rowList, exportParam.getTemplate().getCols());
    }
}
