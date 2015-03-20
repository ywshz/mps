package com.mopote.mps.service;

public class TaskNode {
	private TaskNodeInfo taskNodeInfo;

	public TaskNode(TaskNodeInfo taskNodeInfo) {
		this.taskNodeInfo = taskNodeInfo;
	}

	public TaskNodeInfo getTaskNodeInfo() {
		return taskNodeInfo;
	}

	public void setTaskNodeInfo(TaskNodeInfo taskNodeInfo) {
		this.taskNodeInfo = taskNodeInfo;
	}

}
