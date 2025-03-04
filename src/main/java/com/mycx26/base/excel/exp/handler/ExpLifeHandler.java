package com.mycx26.base.excel.exp.handler;

import com.mycx26.base.excel.exp.bo.ExportParam;

/**
 * export lifecycle handler
 *
 * @author mycx26
 * @date 2025/2/13
 */
public interface ExpLifeHandler {

    String EXP_LIFE_HANDLER = "ExpLifeHandler";

    /**
     * callback when the export task end with success
     *
     * @param exportParam export param
     */
    default void onSuccess(ExportParam exportParam) {
    }

    /**
     * callback when then export task end with failure(error or exception)
     *
     * @param exportParam export param
     */
    default void onFailure(ExportParam exportParam) {
    }
}
