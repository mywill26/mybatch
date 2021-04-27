package com.mycx26.base.excel.exp.readdb.impl;

import com.mycx26.base.constant.PageConstant;
import com.mycx26.base.dao.BaseDao;
import com.mycx26.base.excel.exp.bo.ExportParam;
import com.mycx26.base.excel.exp.readdb.ExportSourceReader;
import com.mycx26.base.excel.property.BatchProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Read and handle data by sql, read data delegate to {@link BaseDao}.
 *
 * Created by mycx26 on 2020/8/20.
 */
@Component
public class SqlDbReader extends ExportSourceReader {

    @Resource
    private BaseDao baseDao;

    @Resource
    private BatchProperty batchProperty;

    @Override
    public List<List<String>> read(int current, ExportParam exportParam) {
        Map<String, Object> params = exportParam.getParams();
        params.put(PageConstant.CURRENT, (current - 1) * exportParam.getBatchCount());
        params.put(PageConstant.SIZE, exportParam.getBatchCount());

        String sqlId = exportParam.getTemplate().getSourceKey();
        List<Map<String, Object>> rowList = baseDao.selectList(sqlId, params);

        return postHandle(rowList, exportParam.getTemplate().getCols());
    }
}
