package com.mycx26.base.excel.service.query;

import com.mycx26.base.service.query.base.PageQuery;
import lombok.Data;

/**
 * Created by mycx26 on 2020/9/22.
 */
@Data
public class ExcelTaskQuery extends PageQuery {

    private String taskStatusCode;

    private String userId;
}
