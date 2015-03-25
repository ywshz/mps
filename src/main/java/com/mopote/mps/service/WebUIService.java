package com.mopote.mps.service;

import com.mopote.mps.domain.FileInfo;
import com.mopote.mps.utils.Constants;
import com.mopote.mps.utils.ZkUtils;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangshu.yang on 2015/3/25.
 */
@Service
public class WebUIService {
    private CuratorFramework client = ZkUtils.newZkClient();

    public List<FileInfo> list(String parent){
        List<FileInfo> files = new ArrayList<FileInfo>();
        List<String> fileNames =  ZkUtils.getChildren(client,parent);
        for(String fileName : fileNames){
            files.add( (FileInfo)ZkUtils.getData(client, Constants.MPS_JOB + parent + Constants.ZK_SEPARATOR + fileName, FileInfo.class));
        }
        return files;
    }
}
