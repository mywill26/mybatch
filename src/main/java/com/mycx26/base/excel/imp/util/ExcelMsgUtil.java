package com.mycx26.base.excel.imp.util;

import java.text.MessageFormat;

/**
 * Created by mycx26 on 2018/8/19.
 */
public class ExcelMsgUtil {

    private static final String COL_MESSAGE = "第【{0}】列【{1}】";

    public static String appendColMsg(int colIndex, String value){
        return MessageFormat.format(COL_MESSAGE, getExcelColIndex(colIndex), value);
    }

    private static String getExcelColIndex(int colIndex) {
        int i = colIndex / 26;
        int j = colIndex % 26;
        StringBuilder str = new StringBuilder();

        for (int k = 1; k <= i; k++) {
            str.append('A');
        }

        return str.toString() + (char) (j + 65);
    }
}
