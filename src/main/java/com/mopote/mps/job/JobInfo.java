package com.mopote.mps.job;

import java.io.Serializable;
import java.util.List;

import com.mopote.mps.enums.EJobType;
import com.mopote.mps.enums.EScheduleStatus;
import com.mopote.mps.enums.EScheduleType;

public class JobInfo implements Serializable {

	private static final long serialVersionUID = -7117031752968173974L;
	private Long id;
	private Long logId;
	private String name;
	private EJobType jobType;
	private EScheduleType scheduleType;
	private EScheduleStatus scheduleStatus;
	private String cron;
	private String dependency;
	private transient List<String> logs;

	public JobInfo() {

	}

	public JobInfo(String name, EJobType jobType, EScheduleType scheduleType,
			EScheduleStatus scheduleStatus, String cron) {
		super();
		this.name = name;
		this.jobType = jobType;
		this.scheduleType = scheduleType;
		this.scheduleStatus = scheduleStatus;
		this.cron = cron;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public Long getLogId() {
		return logId;
	}

	public void setLogId(Long logId) {
		this.logId = logId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EJobType getJobType() {
		return jobType;
	}

	public void setJobType(EJobType jobType) {
		this.jobType = jobType;
	}

	public EScheduleType getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(EScheduleType scheduleType) {
		this.scheduleType = scheduleType;
	}

	public EScheduleStatus getScheduleStatus() {
		return scheduleStatus;
	}

	public void setScheduleStatus(EScheduleStatus scheduleStatus) {
		this.scheduleStatus = scheduleStatus;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public List<String> getLogs() {
		return logs;
	}

	public void setLogs(List<String> logs) {
		this.logs = logs;
	}

	public String getDependency() {
		return dependency;
	}

	public void setDependency(String dependency) {
		this.dependency = dependency;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
