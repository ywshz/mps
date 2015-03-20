package com.mopote.mps.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mopote.mps.enums.EJobRunStatus;
import com.mopote.mps.service.SendJobInfoClient;
import com.mopote.mps.service.TaskNode;
import com.mopote.mps.utils.Constants;
import com.mopote.mps.utils.ZkUtils;

/**
 * Master节点的任务分发器 负责将触发的job分发到指定target
 *
 * @author wangshu.yang
 */
public class DistributionJob implements MpsJob {
    private static Logger logger = LoggerFactory
            .getLogger(DistributionJob.class);

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        Map<String, TaskNode> taskNodes = (Map<String, TaskNode>) context
                .getJobDetail().getJobDataMap().get("TaskNodes");
        AllocationAlgorithm allocAlgori = (AllocationAlgorithm) context
                .getJobDetail().getJobDataMap().get("AllocationAlgorithm");
        CuratorFramework client = (CuratorFramework) context.getJobDetail()
                .getJobDataMap().get("ZkClient");
        JobInfo jobInfo = (JobInfo) context.getJobDetail().getJobDataMap()
                .get("JobInfo");

        logger.debug("初始化计数器{}.", DistributedAtomicLong.class);
        Long newId = generateNewId(client);
        //分配任务的Log ID
        jobInfo.setLogId(newId);
        // 创建job在Zk中的基本信息
        createZkJobLog(client, jobInfo);

        logger.debug("任务 {} 触发,获得所有TaskNode...", jobInfo.getName());

        List<TaskNode> nodeList = new ArrayList<TaskNode>();
        for (Map.Entry<String, TaskNode> entry : taskNodes.entrySet()) {
            nodeList.add(entry.getValue());
        }

        logger.debug("获得{}个TaskNode.", nodeList.size());

        if (nodeList.isEmpty()) {
            logger.info("未获得到可用的TaskNode,Job无法分发,被置为Failed");
            jobFailed(client, jobInfo,
                    "未获得到可用的TaskNode,Job无法分发,被置为Failed");
            return;
        }

        // 准备分发
        SendJobInfoClient sendJob = new SendJobInfoClient();
        int tryTimes = 0;
        // 获得分发目标TaskNode
        do {
            TaskNode target = allocAlgori.allocation(nodeList);
            if (target == null) {
                logger.info("未找到TaskNode或者TaskNode分配算法有误,1秒后重试[第{}次]",
                        tryTimes++);
                try {
                    Thread.sleep(1000);
                    continue;
                } catch (InterruptedException e) {
                }
            }
            // 找到了target
            logger.info("Job:{},分配到Log ID:{},执行服务器:{}",
                    new Object[]{jobInfo.getName(),
                            jobInfo.getLogId(),
                            target.getTaskNodeInfo().getIp()});

            try {
                logger.info("准备分发任务:{},Log ID:{}", jobInfo.getName(),
                        jobInfo.getLogId());

                sendJob.send(target.getTaskNodeInfo(), jobInfo);

                logger.info("任务:{},Log ID:{}, 连接[{}]成功.", new Object[]{jobInfo.getId(), jobInfo.getLogId(), target
                        .getTaskNodeInfo().getIp()});
                logger.info("ZK Running Job中加入{},{}", Constants.RUNNING_JOB_PATH,
                        jobInfo.getLogId());

                String basePath = Constants.RUNNING_JOB_PATH
                        + Constants.ZK_SEPARATOR + jobInfo.getLogId();
                ZkUtils.setData(client, basePath+Constants.ZK_SEPARATOR + "jobId", jobInfo.getId());
                ZkUtils.setData(client, basePath+Constants.ZK_SEPARATOR + "target", target.getTaskNodeInfo());
                ZkUtils.setData(client, basePath+Constants.ZK_SEPARATOR + "startTime", System.currentTimeMillis());
                ZkUtils.setData(client, basePath, Constants.RUNNING);
                break;
            } catch (InterruptedException e) {
                logger.info("目标:{} 无法连接:{},重新选举目标", target.getTaskNodeInfo().getIp(), e.getMessage());
                continue;
            }
        } while (tryTimes <= 10);
        if (tryTimes > 10) {
            jobFailed(client, jobInfo, "任务在规定次数内都无法分发,设置失败.");
        }
        sendJob.close();
    }

    private Long generateNewId(CuratorFramework client) {
        DistributedAtomicLong count = new DistributedAtomicLong(client,
                Constants.JOB_RUN_ID_PATH, new RetryNTimes(10, 10));
        try {
            return count.increment().postValue();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    private void createZkJobLog(CuratorFramework client, JobInfo job) {
        //save job's log, like : /mps/job_logs/job.Id/job.LogId,
        String logPath = Constants.JOB_LOGS + Constants.ZK_SEPARATOR
                + job.getId() + Constants.ZK_SEPARATOR
                + job.getId();
        ZkUtils.create(client, logPath);
        ZkUtils.setData(client, logPath + Constants.ZK_SEPARATOR + "startTime", System.currentTimeMillis());
        ZkUtils.create(client, logPath + Constants.ZK_SEPARATOR + "endTime");
        ZkUtils.setData(client, logPath + Constants.ZK_SEPARATOR + "status",
                EJobRunStatus.RUNNING);
        ZkUtils.create(client, logPath + Constants.ZK_SEPARATOR + "log");
    }

    private void jobFailed(CuratorFramework client, JobInfo job, String msg) {
        String logPath = Constants.JOB_LOGS + Constants.ZK_SEPARATOR
                + job.getId() + Constants.ZK_SEPARATOR
                + job.getId();

        ZkUtils.setStringData(client, logPath + Constants.ZK_SEPARATOR + "log",
                msg + "/nJob Run Failed/n");

        ZkUtils.setData(client, logPath + Constants.ZK_SEPARATOR + "endTime",
                System.currentTimeMillis());

        ZkUtils.setData(client, logPath + Constants.ZK_SEPARATOR + "status", EJobRunStatus.FAILED);
    }
}
