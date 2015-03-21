package com.mopote.mps.utils;

public class Constants {
	public static final String ZK_SEPARATOR = "/";
	public static final String FILE = "file";
	public static final String FOLDER = "folder";
	public static final String RUNNING = "running";
	public static final String SUCCESS = "success";
	public static final String FAILED = "failed";

	public static final String LEADER_SELECTOR_PATH = "/mps/leader_selection";
	//Task Node 的保存路径
	public static final String TASK_NODES_PATH = "/mps/tasknodes";
	//默认的worker数量
	public static final int DEFAULT_WORKER_NUM = 2;
	//用户任务ID分配计数
	public static final String JOB_RUN_ID_PATH = "/mps/job_run_id";
	
	public static final String RUNNING_JOB_PATH = "/mps/running_job";
	
	public static final String MPS_JOB = "/mps/job";

	public static final String JOB_INFO = "/info";
	public static final String JOB_CONTENT = "/content";

	public static final String JOB_LOGS = "/mps/job_logs";
	
	public static final int JOB_MONITOR_PORT = 9988;

	public static final String DEPENDENCY_TREE = "/mps/dependency_tree";

	public static final String DEPENDENCY_LISTENER = "/mps/dependency_listener";

	public static final String JOB_ID_PATH_MAPPING = "/mps/job_path_mapping";
	public static final long JOB_TIME_OUT_MILLIS = 10800; //3 hours
}
