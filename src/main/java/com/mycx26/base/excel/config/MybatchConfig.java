package com.mycx26.base.excel.config;

import com.mycx26.base.annotation.Mapper0;
import com.mycx26.base.dao.impl.BaseDaoImpl;
import com.mycx26.base.service.impl.EnumTypeServiceImpl;
import com.mycx26.base.service.impl.EnumValueServiceImpl;
import com.mycx26.base.util.SpringUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author mycx26
 * @date 2022/3/23
 */
@MapperScan(basePackages = "com.mycx26.base.mapper", annotationClass = Mapper0.class)
@Import({ EnumTypeServiceImpl.class, EnumValueServiceImpl.class })
@MapperScan({"com.mycx26.base.excel.mapper"})
@ComponentScan(basePackages = {"com.mycx26.base.excel"},
        basePackageClasses = {SpringUtil.class, BaseDaoImpl.class})
@Configuration
public class MybatchConfig {
}
