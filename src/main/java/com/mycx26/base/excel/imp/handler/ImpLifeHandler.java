package com.mycx26.base.excel.imp.handler;

import com.mycx26.base.excel.imp.bo.ImportParam;

/**
 * import lifecycle handler
 *
 * @author mycx26
 * @date 2025/2/13
 */
public interface ImpLifeHandler {

    String IMP_LIFE_HANDLER = "ImpLifeHandler";

    /**
     * callback when the import task end
     *
     * @param importParam import param
     */
    default void onEnd(ImportParam importParam) {
    }
}
