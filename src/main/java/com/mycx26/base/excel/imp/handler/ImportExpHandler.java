package com.mycx26.base.excel.imp.handler;

import com.mycx26.base.excel.imp.bo.ImportParam;

/**
 * import exception handler
 *
 * @author mycx26
 * @date 2021/7/5
 */
@FunctionalInterface
public interface ImportExpHandler {

    String EXP_HANDLER = "ExpHandler";

    /**
     * callback when the import task exception
     *
     * @param importParam import param
     */
    void exp(ImportParam importParam);
}
