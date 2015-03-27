package com.mopote.mps.service;

import com.mopote.mps.domain.FileInfo;
import com.mopote.mps.enums.EScheduleType;
import com.mopote.mps.job.JobInfo;
import com.mopote.mps.utils.Constants;
import com.mopote.mps.utils.ZkUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by wangshu.yang on 2015/3/25.
 */
@Service
public class WebUIService {
    private static Logger logger = LoggerFactory.getLogger(WebUIService.class);
    private CuratorFramework client = ZkUtils.newZkClient();

    public WebUIService(){
        client.start();
    }

    public List<FileInfo> list(String parent){
        List<FileInfo> files = new ArrayList<FileInfo>();
        List<String> fileNames =  ZkUtils.getChildren(client,Constants.MPS_JOB + parent);
        for (String fileName : fileNames) {
            files.add((FileInfo) ZkUtils.getData(client, Constants.MPS_JOB + parent + Constants.ZK_SEPARATOR + fileName, FileInfo.class));
        }
        Collections.sort(files, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                int type = o1.getType().compareTo(o2.getType()) ;
                if(type==0){
                    return o1.getCreateTime().compareTo(o2.getCreateTime());
                }
                return type*-1;
            }
        });
        logger.info("list files for {}, side:{}", parent,files.size());
        return files;
    }

    public JobInfo getJobDetail(String parent, String jobName) {
        JobInfo jobInfo = (JobInfo)ZkUtils.getData(client,Constants.MPS_JOB + parent +jobName+Constants.JOB_INFO,JobInfo.class);
        if(jobInfo.getScheduleType()== EScheduleType.DEPENDENCY){
            String real_dependency= "";
            for(String dependency : jobInfo.getDependency().split(",")){
                String path =  ZkUtils.getData(client,Constants.JOB_ID_PATH_MAPPING + "/" + dependency);
                path = path.replaceFirst(Constants.MPS_JOB,"");
                real_dependency+= path + ",";
            }
            real_dependency=real_dependency.substring(0, real_dependency.lastIndexOf(","));
            jobInfo.setRealDependency(real_dependency);
        }
        String ct = ZkUtils.getData(client,Constants.MPS_JOB + parent +jobName+Constants.JOB_CONTENT);
        jobInfo.setScript(ct);
        return jobInfo;
    }

    public void addFolder(String parent, String name){
        ZkUtils.setData(client,Constants.MPS_JOB + parent + Constants.ZK_SEPARATOR +name, new FileInfo(name,new Date(),new Date(), Constants.FOLDER));
    }

    public void addTask(String parent, JobInfo jobInfo){

    }
}
