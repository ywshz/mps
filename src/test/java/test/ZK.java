package test;

import com.mopote.mps.domain.FileInfo;
import com.mopote.mps.enums.EJobType;
import com.mopote.mps.enums.EScheduleStatus;
import com.mopote.mps.enums.EScheduleType;
import com.mopote.mps.job.JobInfo;
import com.mopote.mps.utils.Constants;
import com.mopote.mps.utils.ZkUtils;
import org.apache.curator.framework.CuratorFramework;

import java.util.Date;

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
		ZkUtils.create(client, Constants.RUNNING_JOB_PATH);
		ZkUtils.create(client, Constants.DEPENDENCY_LISTENER);
		ZkUtils.create(client, Constants.LEADER_SELECTOR_PATH);
		ZkUtils.create(client, Constants.DEPENDENCY_TREE);


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
		j2.setCron("0 */1 * * * ?");
		j2.setScheduleType(EScheduleType.CRON);
		j2.setScheduleStatus(EScheduleStatus.ON);
		j2.setJobType(EJobType.SHELL);

		JobInfo j3 = new JobInfo();
		j3.setId(3L);
		j3.setName("job3");
		j3.setCron("*/30 * * * * ?");
		j3.setScheduleType(EScheduleType.CRON);
		j3.setScheduleStatus(EScheduleStatus.ON);
		j3.setJobType(EJobType.SHELL);

		ZkUtils.setData(client, "/mps/job/job1/info", j);
		ZkUtils.setData(client, "/mps/job/job2/info", j2);
		ZkUtils.setData(client, "/mps/job/job3/info", j3);

		ZkUtils.setStringData(client, "/mps/job/job1/content", "echo job1");
		ZkUtils.setStringData(client, "/mps/job/job2/content", "echo job2");
		ZkUtils.setStringData(client, "/mps/job/job3/content", "echo job3");

		ZkUtils.setData(client, "/mps/job/job1", new FileInfo("job1", EScheduleStatus.OFF, new Date(), new Date(), "file"));
		ZkUtils.setData(client, "/mps/job/job2", new FileInfo("job2", EScheduleStatus.OFF, new Date(),new Date(),"file"));
		ZkUtils.setData(client, "/mps/job/job3", new FileInfo("job3", EScheduleStatus.ON, new Date(), new Date(), "file"));
        ZkUtils.setData(client, "/mps/job/folder1", new FileInfo("folder1", new Date(), new Date(), "folder"));
        ZkUtils.setData(client, "/mps/job/folder1/folder2", new FileInfo("folder2", new Date(), new Date(), "folder"));

        ZkUtils.setStringData(client, "/mps/job_path_mapping/1", "/mps/job/job1");
		ZkUtils.setStringData(client, "/mps/job_path_mapping/2", "/mps/job/job2");
		ZkUtils.setStringData(client, "/mps/job_path_mapping/3", "/mps/job/job3");

	}
}
