package com.mycx26.base.excel.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for mybatch framework.
 *
 * @author mycx26
 * @date 2021/4/27
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "mybatch")
@Component
public class BatchProperty {

    private int impMaxCount = 9;

    private int impBatchCount = 2000;

    private int expBatchCount = 5000;
}
