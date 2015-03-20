package test;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;

import com.mopote.mps.enums.EJobType;
import com.mopote.mps.enums.EScheduleStatus;
import com.mopote.mps.enums.EScheduleType;
import com.mopote.mps.job.JobInfo;
import com.mopote.mps.service.MasterNode;
import com.mopote.mps.service.TaskNodeInfo;
import com.mopote.mps.utils.Constants;
import com.mopote.mps.utils.ZkUtils;

public class ZK {

	public static void main(String[] args) throws Exception {
		CuratorFramework client = ZkUtils.newZkClient();
		
		client.start();
		
//		TaskNodeInfo info = new TaskNodeInfo();
//		info.setIp("123");
//		info.setName("123");
//		ZkUtils.setData(client, Constants.RUNNING_JOB_PATH+"/"+999, info);
		
//		DistributedAtomicLong count = new DistributedAtomicLong(client, Constants.JOB_RUN_ID_PATH, new RetryNTimes(10, 10));
//		
//		System.out.println(count.increment().postValue());
		
//		System.out.println(EScheduleStatus.ON.ordinal());
//		List<String> jobs = client.getChildren().forPath("/mps/job/folder1");
//		for(String jobName : jobs){
//			JobInfo j = (JobInfo)ZkUtils.getData(client, "/mps/job/"+jobName+"/info", JobInfo.class);
//			System.out.println(j.getName()+" "+ j.getCron() + " " +j.getScheduleStatus() );
//			j.setCron("*/5 * * * * ?");
//			j.setScheduleType(EScheduleType.CRON);
//			j.setScheduleStatus(EScheduleStatus.ON);
//			j.setJobType(EJobType.SHELL);
//			ZkUtils.setData(client, "/mps/job/"+jobName+"/info", j);
//		}
		
		JobInfo j = new JobInfo();
		j.setId(1L);
		j.setName("job1");
		j.setScheduleType(EScheduleType.DEPENDENCY);
		j.setScheduleStatus(EScheduleStatus.ON);
		j.setJobType(EJobType.SHELL);
		j.setDependency("2,3");

		JobInfo j2 = new JobInfo();
		j2.setId(2L);
		j2.setName("job2");
		j2.setCron("*/10 * * * * ?");
		j2.setScheduleType(EScheduleType.CRON);
		j2.setScheduleStatus(EScheduleStatus.ON);
		j2.setJobType(EJobType.SHELL);

		JobInfo j3 = new JobInfo();
		j3.setId(3L);
		j3.setName("job3");
		j3.setCron("*/20 * * * * ?");
		j3.setScheduleType(EScheduleType.CRON);
		j3.setScheduleStatus(EScheduleStatus.ON);
		j3.setJobType(EJobType.SHELL);

		ZkUtils.setData(client, "/mps/job/job1/info", j);
		ZkUtils.setData(client, "/mps/job/job2/info", j2);
		ZkUtils.setData(client, "/mps/job/job3/info", j3);

		ZkUtils.setData(client,"/mps/job/job1/content","I'm job1");
		ZkUtils.setData(client,"/mps/job/job2/content","I'm job2");
		ZkUtils.setData(client,"/mps/job/job3/content","I'm job3");
	}
}
