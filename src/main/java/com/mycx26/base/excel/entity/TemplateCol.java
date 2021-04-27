package com.mycx26.base.excel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * excel模板列
 * </p>
 *
 * @author mycx26
 * @since 2020-08-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ie_template_col")
public class TemplateCol implements Serializable {

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
     * 列导入表名
     */
    private String tableName;

    /**
     * 列名称
     */
    private String colName;

    /**
     * 列label
     */
    private String colLabel;

    /**
     * 列类型code
     */
    private String colTypeCode;

    /**
     * 是否必填
     */
    @TableField("is_required")
    private Boolean required;

    /**
     * 是否唯一
     */
    @TableField("is_uniqueness")
    private Boolean uniqueness;

    /**
     * 枚举类型code
     */
    private String enumTypeCode;

    /**
     * 顺序号
     */
    private Integer orderNo;

    /**
     * 子枚举顺序号
     */
    private Integer childOrderNo;

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

}
