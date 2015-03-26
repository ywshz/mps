package com.mopote.mps.domain;

import com.mopote.mps.enums.EScheduleStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by wangshu.yang on 2015/3/25.
 */
public class FileInfo implements Serializable {
    private String name;
    private EScheduleStatus status;
    private Date createTime;
    private Date modifyTime;
    private String type;

    public FileInfo() {

    }

    public FileInfo(String name, Date createTime, Date modifyTime, String type) {
        this.name = name;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.type = type;
    }

    public FileInfo(String name,EScheduleStatus status, Date createTime, Date modifyTime, String type) {
        this.name = name;
        this.status = status;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(EScheduleStatus status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
