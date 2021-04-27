package com.mycx26.base.excel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mycx26.base.excel.imp.validator.base.ValueValidator;
import com.mycx26.base.excel.imp.validator.template.TemplateValidator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * excel模板
 * </p>
 *
 * @author mycx26
 * @since 2020-08-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ie_template")
public class Template implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板code
     */
    private String tmplCode;

    /**
     * 模板类别code
     */
    private String categoryCode;

    /**
     * 模板名称
     */
    private String tmplName;

    /**
     * 模版文件名
     */
    private String fileName;

    /**
     * 模板路径
     */
    private String tmplPath;

    /**
     * 描述
     */
    private String description;

    /**
     * import: 解析开始行, from 0
     */
    private Integer startRow;

    /**
     * import: 是否有唯一性
     */
    private Boolean isUniqueness;

    /**
     * import: 是否写数据库
     */
    private Boolean isWriteDb;

    /**
     * import: 写db策略code
     */
    private String dbStrategyCode;

    /**
     * import: 是否回滚, rollback
     */
    private Boolean isRoolback;

    /**
     * export: 导出数据来源code
     */
    private String sourceCode;

    /**
     * export: 导出数据来源key
     */
    private String sourceKey;

    /**
     * 创建人id
     */
    private String creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    private String modifierId;

    /**
     * 修改时间
     */
    private LocalDateTime modifyTime;

    /**
     * 可用标记
     */
    private Boolean yn;

    //=============================================================================================>

    /**
     * 模板列
     */
    @TableField(exist = false)
    private List<TemplateCol> cols;

    /**
     * import: column type & order number map
     */
    @TableField(exist = false)
    private Map<String, List<Integer>> colTypeOrdersMap = new HashMap<>(6);

    /**
     * import: required column order numbers
     */
    @TableField(exist = false)
    private List<Integer> requiredOrders = new ArrayList<>(10);

    /**
     * import: base value validators
     */
    @TableField(exist = false)
    private List<ValueValidator> validators = new ArrayList<>(6);

    /**
     * import: template validator
     */
    @TableField(exist = false)
    private TemplateValidator templateValidator;

    /**
     * import: Unique column order number if exists.
     */
    @TableField(exist = false)
    private Integer uniqueOrderNo;

    /**
     * import: table name - ( col name - col order no )
     */
    @TableField(exist = false)
    private Map<String, Map<String, Integer>> tblColOrderMap = new HashMap<>(2);

    /**
     * import: table name - col name list
     */
    @TableField(exist = false)
    private Map<String, List<String>> tblColNames = new HashMap<>(2);

    /**
     * import: table name - col order list
     */
    @TableField(exist = false)
    private Map<String, List<Integer>> tblColOrders = new HashMap<>(2);
}
