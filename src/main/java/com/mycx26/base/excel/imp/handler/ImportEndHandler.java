package com.mycx26.base.excel.imp.handler;

import com.mycx26.base.excel.imp.bo.ImportParam;

/**
 * import end handler
 *
 * @author mycx26
 * @date 2021/7/5
 */
@FunctionalInterface
public interface ImportEndHandler {

    /**
     * callback when the import task end with success
     *
     * @param importParam import param
     */
    void end(ImportParam importParam);
}
