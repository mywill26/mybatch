package com.mycx26.base.excel.util;

import com.mycx26.base.constant.Symbol;
import com.mycx26.base.excel.constant.ExcelConst;
import com.mycx26.base.util.DateFormatUtil;

/**
 * Created by mycx26 on 2020/8/12.
 */
public class ExcelUtil {

    public static String rename(String fileName) {
        return fileName
                + Symbol.UNDERLINE
                + DateFormatUtil.getNowMsTight()
                + ExcelConst.EXCEL_2007_SUFFIX;
    }
}
