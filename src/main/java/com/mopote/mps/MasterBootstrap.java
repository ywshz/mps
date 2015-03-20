package com.mopote.mps;

import java.io.IOException;
import java.util.UUID;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mopote.mps.service.MasterNode;
import com.mopote.mps.utils.Constants;
import com.mopote.mps.utils.ZkUtils;

/**
 * 用于启动Master，TaskNode，是程序的起始点
 * 
 * @author ywshz@vip.qq.com
 *
 */
public class MasterBootstrap {
	private static Logger logger = LoggerFactory
			.getLogger(MasterBootstrap.class);
	private static final String MASTER_NAME = UUID.randomUUID().toString();

	public static void main(String[] args) {
		logger.info("创建Zookeeper连接");
		// 启动MasterNode，去争抢Leader权限
		CuratorFramework zkClient = ZkUtils.newZkClient();
		zkClient.start();
		
		MasterNode master = new MasterNode(zkClient,
				Constants.LEADER_SELECTOR_PATH, MASTER_NAME);

		try {
			logger.info("成功连接到Zookeeper");
			master.start();

			while (true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					break;
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			CloseableUtils.closeQuietly(master);
			CloseableUtils.closeQuietly(zkClient);
		}
	}

}
