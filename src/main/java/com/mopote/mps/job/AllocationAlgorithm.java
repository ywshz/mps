package com.mopote.mps.job;

import java.util.List;

import com.mopote.mps.service.TaskNode;

public interface AllocationAlgorithm {
	
	public TaskNode allocation(List<TaskNode> nodes);
	
}
