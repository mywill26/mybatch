package com.mycx26.base.excel.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author mycx26
 * @date 2022/3/23
 */
@MapperScan({"com.mycx26.base.excel.mapper"})
@ComponentScan(basePackages = {"com.mycx26.base.excel"})
@Configuration
public class MybatchConfig {
}
