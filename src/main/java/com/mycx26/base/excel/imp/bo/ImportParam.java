package com.mycx26.base.excel.imp.bo;

import com.mycx26.base.excel.entity.Template;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by mycx26 on 2018/8/16.
 */
public class ImportParam {

    private String userId;
    private Long taskId;
    private Template template;

    private Map<String, Object> params;     // business params to participate import

    private volatile boolean error = false;
    private volatile boolean exception = false;

    private int successCount;
    private int failureCount;
    private int totalCount;

    private String writePath;

    private String expDesc;

    private int batchCount;

    private List<CompletableFuture<String>> cfs;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isException() {
        return exception;
    }

    public void setException(boolean exception) {
        this.exception = exception;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getWritePath() {
        return writePath;
    }

    public void setWritePath(String writePath) {
        this.writePath = writePath;
    }

    public String getExpDesc() {
        return expDesc;
    }

    public void setExpDesc(String expDesc) {
        this.expDesc = expDesc;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

    public List<CompletableFuture<String>> getCfs() {
        return cfs;
    }

    public void setCfs(List<CompletableFuture<String>> cfs) {
        this.cfs = cfs;
    }

    public void addSuccessCount(){
        successCount += 1;
    }

    public void addFailureCount(){
        failureCount += 1;
    }

    public void addTotalCount(){
        totalCount += 1;
    }
}
