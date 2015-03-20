package com.mopote.mps.job;

import java.util.List;
import java.util.Random;

import com.mopote.mps.service.TaskNode;

public class RandomAllocationAlgorithm implements AllocationAlgorithm {
	private Random rand = new Random();

	public TaskNode allocation(List<TaskNode> nodes)  {
		try{
			int index = rand.nextInt(nodes.size());
			return nodes.get(index);
		}catch(Exception e){
			return null;
		}
	}

}
