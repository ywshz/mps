package com.mopote.mps.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mopote.mps.enums.EJobRunStatus;
import com.mopote.mps.job.JobInfo;
import com.mopote.mps.utils.Constants;
import com.mopote.mps.utils.DateRender;
import com.mopote.mps.utils.ZkUtils;

public class Worker implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(Worker.class);
	private final String HIVE_HOME;
	private final String HADOOP_HOME;
	private final int MAX_STORE_LINES = 10000;

	private final String WORK_FOLDER;
	// private final String BASE_UPLOAD_PATH;
	// private final String FILE_TYPE;

	private static BlockingQueue<JobInfo> jobQueue = new ArrayBlockingQueue<JobInfo>(
			1000);
	private CuratorFramework zkClient;

	public Worker(CuratorFramework zkClient) {
		Properties props = new Properties();
		try {
			props.load(Worker.class
					.getResourceAsStream("/mps-config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		WORK_FOLDER = (String) props.get("work.folder");
		// BASE_UPLOAD_PATH = (String) props.get("BASE_UPLOAD_PATH");
		// FILE_TYPE = (String) props.get("FILE_TYPE");
		HIVE_HOME = (String) props.get("HIVE_HOME");
		HADOOP_HOME = (String) props.get("HADOOP_HOME");

		this.zkClient = zkClient;
	}

	public static void addJob(JobInfo job) throws InterruptedException {
		jobQueue.put(job);
	}

	public static JobInfo take() throws InterruptedException {
		return jobQueue.take();
	}

	public void run() {
		JobInfo job = null;
		while (true) {
			try {
				job = jobQueue.take();
				logger.info("开始执行任务 : {}", job.getName());
				execute(job);
			} catch (InterruptedException e) {

			} finally {
				cleanup(job);
			}

		}
	}

	private void cleanup(JobInfo job) {
		ZkUtils.delete(zkClient, Constants.RUNNING_JOB_PATH
				+ Constants.ZK_SEPARATOR + job.getLogId());

		File folder = new File(WORK_FOLDER + File.separator + job.getLogId());
		// 因为folder下都是file, 不会再有folder, 可直接遍历删除
		for (File file : folder.listFiles()) {
			file.delete();
		}
		folder.delete();
	}

	private void execute(JobInfo job) {
		// 在本地建立运行目录
		String runFolderAbsPath = createRunFolder(job);
		// 从ZK中得到脚本
		String jobRealPath = ZkUtils.getData(zkClient, Constants.JOB_ID_PATH_MAPPING + Constants.ZK_SEPARATOR + job.getId());
		String script = ZkUtils.getData(zkClient, jobRealPath + Constants.JOB_CONTENT);
		// 把脚本写入工作目录中
		String runFileAbsPath = null;
		try {
			runFileAbsPath = createJobFiles(job, script, runFolderAbsPath);
		} catch (IOException e) {
			error(job.getLogId(), "创建脚本文件失败:" + e.getMessage());
			return;
		}

		log(job.getLogId(), "开始执行脚本:");
		log(job.getLogId(), script);

		ProcessBuilder builder = null;
		switch (job.getJobType()) {
		case HIVE:
			builder = new ProcessBuilder(HIVE_HOME + "/bin/hive", "-f",
					runFileAbsPath);
			builder.directory(new File(runFolderAbsPath));
			builder.environment().put("HADOOP_HOME", HADOOP_HOME);
			builder.environment().put("HIVE_HOME", HIVE_HOME);
			break;
		case SHELL:
			builder = new ProcessBuilder(runFileAbsPath);
			builder.directory(new File(runFolderAbsPath));
			break;
		}

		backgroundWorking(builder, job.getLogId());
	}

	private void backgroundWorking(ProcessBuilder builder, final Long logId) {
		Process process = null;
		try {
			process = builder.start();
		} catch (IOException e) {
			error(logId, "执行环境启动失败:" + e.getMessage());
			return;
		}

		final InputStream inputStream = process.getInputStream();
		final InputStream errorStream = process.getErrorStream();
		final AtomicInteger lineCount = new AtomicInteger(0);
		Thread normal = new Thread() {
			@Override
			public void run() {
				try {
					InputStreamReader isr = new InputStreamReader(inputStream);
					BufferedReader br = new BufferedReader(isr);
					String line = null;
					while ((line = br.readLine()) != null) {
						int curr = lineCount.getAndIncrement();
						if (curr < MAX_STORE_LINES) {
							log(logId, line);
						} else if (curr == MAX_STORE_LINES) {
							log(logId, "该任务LOG已有1万条,为减少内存占用,停止记录");
						}
					}
				} catch (IOException ioE) {
					ioE.printStackTrace();
				}
			}
		};

		Thread error = new Thread() {
			@Override
			public void run() {
				try {
					InputStreamReader isr = new InputStreamReader(errorStream);
					BufferedReader br = new BufferedReader(isr);
					String line = null;
					while ((line = br.readLine()) != null) {
						int curr = lineCount.getAndIncrement();
						if (curr < MAX_STORE_LINES) {
							log(logId, line);
						} else if (curr == MAX_STORE_LINES) {
							log(logId, "该任务LOG已有1万条,为减少内存占用,停止记录");
						}
					}
				} catch (IOException ioE) {
					ioE.printStackTrace();
				}
			}
		};

		normal.start();
		error.start();

		while (normal.isAlive() || error.isAlive()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		int exitCode = -999;
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			process = null;
		}

		if (exitCode == 0) {
			log(logId, "Job Run Success");
			ZkUtils.setData(zkClient, Constants.JOB_LOGS
					+ Constants.ZK_SEPARATOR + logId + Constants.ZK_SEPARATOR
					+ "endTime", System.currentTimeMillis());
			ZkUtils.setData(zkClient, Constants.JOB_LOGS
					+ Constants.ZK_SEPARATOR + logId + Constants.ZK_SEPARATOR
					+ "status", EJobRunStatus.SUCCESS);

			ZkUtils.setStringData(zkClient, Constants.RUNNING_JOB_PATH + Constants.ZK_SEPARATOR + logId, Constants.SUCCESS);
		} else {
			log(logId, "Job Run Failed");

			ZkUtils.setData(zkClient, Constants.JOB_LOGS
					+ Constants.ZK_SEPARATOR + logId + Constants.ZK_SEPARATOR
					+ "endTime", System.currentTimeMillis());
			ZkUtils.setData(zkClient, Constants.JOB_LOGS
					+ Constants.ZK_SEPARATOR + logId + Constants.ZK_SEPARATOR
					+ "status", EJobRunStatus.FAILED);

			ZkUtils.setStringData(zkClient, Constants.RUNNING_JOB_PATH + Constants.ZK_SEPARATOR + logId, Constants.FAILED);
		}
	}

	private String createJobFiles(JobInfo job, String script,
			String runFolderAbsPath) throws IOException {
		File file = null;
		script = DateRender.render(script);
		file = new File(runFolderAbsPath + File.separator + job.getLogId()
				+ ".bat");
		file.createNewFile();
		file.setExecutable(true);
		file.setReadable(true);
		file.setWritable(true);
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(script);
		out.close();

		return file.getAbsolutePath();
	}

	private String createRunFolder(JobInfo job) {
		File folder = new File(WORK_FOLDER + File.separator + job.getLogId());
		folder.mkdirs();
		return folder.getAbsolutePath();
	}

	private void log(Long logId, String newLog) {
		String old = ZkUtils.getData(this.zkClient, Constants.JOB_LOGS
				+ Constants.ZK_SEPARATOR + logId + Constants.ZK_SEPARATOR
				+ "log");
		ZkUtils.setStringData(zkClient, Constants.JOB_LOGS
				+ Constants.ZK_SEPARATOR + logId + Constants.ZK_SEPARATOR
				+ "log", old + newLog + "/n");
	}

	private void error(Long logId, String msg) {
		log(logId, msg);
		log(logId, "Job Run Failed");

		ZkUtils.setData(zkClient, Constants.JOB_LOGS + Constants.ZK_SEPARATOR
				+ logId + Constants.ZK_SEPARATOR + "endTime",
				System.currentTimeMillis());
		ZkUtils.setData(zkClient, Constants.JOB_LOGS + Constants.ZK_SEPARATOR
				+ logId + Constants.ZK_SEPARATOR + "status",
				EJobRunStatus.FAILED);

		ZkUtils.setStringData(zkClient, Constants.RUNNING_JOB_PATH + Constants.ZK_SEPARATOR + logId, Constants.FAILED);
	}
}
