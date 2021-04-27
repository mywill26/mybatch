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
 * excel任务
 * </p>
 *
 * @author mycx26
 * @since 2020-08-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ie_excel_task")
public class ExcelTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务流水号
     */
    private String flowNo;

    /**
     * 任务类型
     */
    private String taskTypeCode;

    /**
     * 模板类型code
     */
    private String tmplTypeCode;

    /**
     * 导入文件名
     */
    private String impFileName;

    /**
     * 导入文件路径
     */
    private String impFilePath;

    /**
     * 导出文件名
     */
    private String expFileName;

    /**
     * 导出文件路径
     */
    private String expFilePath;

    /**
     * 任务用户id
     */
    private String userId;

    /**
     * 任务状态code
     */
    private String taskStatusCode;

    /**
     * 是否有错误
     */
    @TableField("is_error")
    private Boolean error;

    /**
     * 是否有异常
     */
    @TableField("is_exception")
    private Boolean exception;

    /**
     * 成功条数
     */
    private Integer successCount;

    /**
     * 失败条数
     */
    private Integer failureCount;

    /**
     * 总条数
     */
    private Integer totalCount;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务信息
     */
    private String description;

    /**
     * 导入参数
     */
    private String params;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime modifyTime;

    @TableField(exist = false)
    private String taskType;

    @TableField(exist = false)
    private String tmplType;

    @TableField(exist = false)
    private String taskStatus;
}
