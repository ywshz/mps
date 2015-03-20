package com.mopote.mps.service;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.utils.ZKPaths;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mopote.mps.enums.EScheduleStatus;
import com.mopote.mps.enums.EScheduleType;
import com.mopote.mps.job.AllocationAlgorithm;
import com.mopote.mps.job.DistributionJob;
import com.mopote.mps.job.JobInfo;
import com.mopote.mps.job.RandomAllocationAlgorithm;
import com.mopote.mps.utils.Constants;
import com.mopote.mps.utils.ZkUtils;

public class MasterNode extends LeaderSelectorListenerAdapter implements
		Closeable {
	private static Logger logger = LoggerFactory.getLogger(MasterNode.class);
	private final String name;
	private final LeaderSelector leaderSelector;
	private PathChildrenCache cache;
	private AllocationAlgorithm allocAlgori = null;
	private Gson gson = new Gson();
	private Map<String, TaskNode> taskNodes = new ConcurrentHashMap<String, TaskNode>();
	private SchedulerFactory schedulerFactory;
	private Scheduler scheduler;

	public MasterNode(CuratorFramework client, String path, String name) {
		this.name = name;
		logger.info("Master Node Name set to {}", this.name);

		leaderSelector = new LeaderSelector(client, path, this);
		leaderSelector.autoRequeue();
		logger.info("创建Master LeaderSelector");

		allocAlgori = new RandomAllocationAlgorithm();
		logger.info("任务分配算法被设置为{}", RandomAllocationAlgorithm.class);

		// 获得所有TaskNode
		try {
			addTaskNodeListener(client);
			initScheduler();
			initJobs(client);
		} catch (Exception e1) {
			logger.error("初始化MasterNode发生异常:{}", e1.getMessage());
		}

		client.getConnectionStateListenable().addListener(
				new ConnectionStateListener() {

					public void stateChanged(CuratorFramework client,
							ConnectionState newState) {
						System.exit(-1);
					}
				});

		// 在JVM异常退出后，关闭leadership
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					leaderSelector.interruptLeadership();
					cache.close();
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		});

	}

	public void start() throws IOException {
		logger.info("启动MasterNode,开始争抢Leader位置");
		leaderSelector.start();
	}

	public void close() throws IOException {
		logger.info("关闭MasterNode");
		leaderSelector.close();
	}

	public void takeLeadership(CuratorFramework client) throws Exception {
		final int waitSeconds = 3;
		logger.info("{} 获得了 Leader 位置, 开始执政.", this.name);
		try {
			startRunningJobListner(client);
			// 启动调度器
			this.scheduler.start();
			// 保持占有leader锁
			while (true) {
				Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
			}
		} catch (InterruptedException e) {
			logger.error("{} was interrupted with {}", name, e.getMessage());
			Thread.currentThread().interrupt();
		} finally {
			logger.info("{} relinquishing leadership.", name);
		}
	}

	/**
	 * 监控任务超时，监控任务依赖关系树
	 * @param client
	 */
	private void startRunningJobListner(CuratorFramework client) {
		List<String> runningJobs = ZkUtils.getChildren(client, Constants.RUNNING_JOB_PATH);
		for(String jobStr : runningJobs){
			String value = ZkUtils.getData(client, Constants.RUNNING_JOB_PATH + Constants.ZK_SEPARATOR + jobStr);
			if(Constants.RUNNING.equals(value)){
				//TODO: 检查任务超时
			}else if(Constants.SUCCESS.equals(value)){
				//TODO: 检查依赖
				
			}else if(Constants.FAILED.equals(value)){
				//TODO: 任务失败后续处理，如发邮件
			}
		}
	}

	private void initScheduler() throws SchedulerException {
		logger.info("为 {} 设置Scheduler {}", this.name, StdSchedulerFactory.class);
		this.schedulerFactory = new StdSchedulerFactory();
		this.scheduler = schedulerFactory.getScheduler();
	}

	private void initJobs(CuratorFramework client) {
		logger.info("开始初始化所有被置为ON的Job");
		try {
			iterateJobs(client, Constants.MPS_JOB);
		} catch (Exception e) {
			logger.error("初始化Job失败,失败原因:{}", e.getMessage());
		}
	}

	/**
	 * 递归完成file的遍历
	 * @param client
	 * @param path
	 * @throws SchedulerException
	 */
	private void iterateJobs(CuratorFramework client, String path)
			throws SchedulerException {
		List<String> jobs = ZkUtils.getChildren(client, path);
		for (String jobName : jobs) {
			String fullPath = path + Constants.ZK_SEPARATOR + jobName;
			if (Constants.FILE.equals(ZkUtils.getData(client, fullPath))) {
				JobInfo j = (JobInfo) ZkUtils.getData(client, fullPath
						+ Constants.JOB_INFO, JobInfo.class);
				j.setName(path + Constants.ZK_SEPARATOR + j.getName());
				if (j.getScheduleStatus() == EScheduleStatus.ON) {
					scheduleJob(client, j);
				}
			} else {
				iterateJobs(client, fullPath);
			}
		}
	}

	private void scheduleJob(CuratorFramework client, JobInfo jobInfo)
			throws SchedulerException {
		if (EScheduleType.CRON == jobInfo.getScheduleType()) {
			CronTrigger trigger = newTrigger().withIdentity(jobInfo.getName())
					.withSchedule(cronSchedule(jobInfo.getCron())).build();

			JobDetail job = JobBuilder.newJob(DistributionJob.class)
					.withIdentity(jobInfo.getName()).build();
			job.getJobDataMap().put("TaskNodes", this.taskNodes);
			job.getJobDataMap().put("AllocationAlgorithm", this.allocAlgori);
			job.getJobDataMap().put("JobInfo", jobInfo);
			job.getJobDataMap().put("ZkClient", client);
			scheduler.scheduleJob(job, trigger);

			logger.info("{}被设置为自动运行,运行方式:{},运行频率:{}",
					new Object[] { jobInfo.getName(),
							jobInfo.getScheduleType(), jobInfo.getCron() });
		} else {
			// TODO:
			logger.info(
					"{}被设置为自动运行,运行方式:{},依赖关系是:{}",
					new Object[] { jobInfo.getName(),
							jobInfo.getScheduleType(), jobInfo.getDependency() });

			String[] dependencies = jobInfo.getDependency().split(",");
			//build dependency tree and listener tree
			for(String dependency : dependencies){
				String np = Constants.DEPENDENCY_TREE + Constants.ZK_SEPARATOR + jobInfo.getId();
				if(!ZkUtils.exist(client,np)){
					ZkUtils.create(client,np+Constants.ZK_SEPARATOR+dependency);
				}
				String lp = Constants.DEPENDENCY_LISTENER + Constants.ZK_SEPARATOR + dependency;
				if(!ZkUtils.exist(client,lp)){
					ZkUtils.create(client,lp+Constants.ZK_SEPARATOR+jobInfo.getId());
				}
			}
		}
	}

	private void addTaskNodeListener(CuratorFramework client) throws Exception {
		logger.info("从Zookeeper中获得已注册的TaskNode");
		cache = new PathChildrenCache(client, Constants.TASK_NODES_PATH, true);
		cache.start();
		PathChildrenCacheListener listener = new PathChildrenCacheListener() {

			public void childEvent(CuratorFramework client,
					PathChildrenCacheEvent event) throws Exception {
				switch (event.getType()) {
				case CHILD_ADDED: {
					String path = ZKPaths.getNodeFromPath(event.getData()
							.getPath());
					String value = new String(event.getData().getData());
					logger.info("TaskNode: {}, value: {}被发现,并加入TaskNode列表",
							path, value);

					TaskNodeInfo tni = gson.fromJson(value, TaskNodeInfo.class);
					taskNodes.put(tni.getIp(), new TaskNode(tni));

					break;
				}
				case CHILD_UPDATED: {
					String path = ZKPaths.getNodeFromPath(event.getData()
							.getPath());
					String value = new String(event.getData().getData());
					logger.info("TaskNode: {}, 内容更新为 : {}", path, value);
					TaskNodeInfo tni = gson.fromJson(value, TaskNodeInfo.class);
					taskNodes.put(tni.getIp(), new TaskNode(tni));

					break;
				}
				case CHILD_REMOVED: {
					String path = ZKPaths.getNodeFromPath(event.getData()
							.getPath());
					String value = new String(event.getData().getData());
					logger.info("TaskNode : {}, value: {} 已经移除", path, value);
					TaskNodeInfo tni = gson.fromJson(value, TaskNodeInfo.class);
					taskNodes.remove(tni.getIp());
					//TODO: 重新分配任务
					break;
				}
				default:
					String path = ZKPaths.getNodeFromPath(event.getData()
							.getPath());
					logger.info("other events: {}", path);
				}
			}
		};
		cache.getListenable().addListener(listener);
	}

	// @Override
	// public void stateChanged(CuratorFramework client, ConnectionState
	// newState) {
	// super.stateChanged(client, newState);
	// logger.info("{} state changed to {}", name, newState.name());
	// if ((newState == ConnectionState.SUSPENDED)
	// || (newState == ConnectionState.LOST)) {
	// logger.info("{} state SUSPENDED, release leadership", name);
	// leaderSelector.interruptLeadership();
	// try {
	// this.scheduler.standby();
	// } catch (SchedulerException e) {
	// e.printStackTrace();
	// }
	// }
	// }
}