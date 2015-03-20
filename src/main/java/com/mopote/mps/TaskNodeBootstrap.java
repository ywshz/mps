package com.mopote.mps;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mopote.mps.service.ReceiveJobServer;
import com.mopote.mps.service.TaskNodeInfo;
import com.mopote.mps.service.Worker;
import com.mopote.mps.utils.Constants;
import com.mopote.mps.utils.ZkUtils;

public class TaskNodeBootstrap implements Runnable {
	public static void main(String[] args) throws Exception {

		Thread taskNodeBootstrap = new Thread(new TaskNodeBootstrap());
		taskNodeBootstrap.start();

		while (true) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	private static Logger logger = LoggerFactory
			.getLogger(TaskNodeBootstrap.class);
	private static final String TASK_NODE_NAME = UUID.randomUUID().toString();
	private static Gson gson = new Gson();
	private String registerIp = null;
	private List<Worker> workers = new ArrayList<Worker>();
	
	public void run() {
		final CuratorFramework zkClient = ZkUtils.newZkClient();
		zkClient.start();
		logger.info("启动TaskNode : {}", TASK_NODE_NAME);
		initWorkers(zkClient);
		try {
			//注册TaskNode
			registerTaskNode(zkClient);
		} catch (UnknownHostException e1) {
			logger.error(e1.getMessage());
		} catch (SocketException e1) {
			logger.error(e1.getMessage());
		} catch (Exception e1) {
			logger.error(e1.getMessage());
		}

		//开启监听Job分配
		startJobListener();
	}

	private void startJobListener() {
		logger.info("开始监听任务开发");
		ReceiveJobServer jobServer = new ReceiveJobServer();
		try {
			jobServer.start();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
	}

	private void registerTaskNode(final CuratorFramework zkClient)
			throws UnknownHostException, SocketException, Exception {
		logger.info("准备注册TaskNode: {}", TASK_NODE_NAME);
		InetAddress ia = InetAddress.getLocalHost();
		final String ip = getRealIp();
		TaskNodeInfo info = new TaskNodeInfo();
		info.setIp(ip);
		info.setName(ia.getHostName());
		info.setPort(Constants.JOB_MONITOR_PORT);
		info.setRegister_time(System.currentTimeMillis());
		String json = gson.toJson(info);
		logger.info("TaskNode注册信息为: {}", json);

		int tryTimes = 1;
		do{
			if(zkClient.checkExists().forPath(Constants.TASK_NODES_PATH + "/" + info.getIp())!=null){
				logger.info("[第{}次]该节点注册信息已经存在,1秒后重试...",tryTimes++);
				Thread.sleep(1000);
			}else{
				break;
			}
		}while(tryTimes<=20);
		
		if(tryTimes>20){
			logger.error("该机器已经有TaskNode,不能再开启一个新的TaskNode");
			System.exit(-1);
		}
		
		zkClient.create()
				.withMode(CreateMode.EPHEMERAL)
				.forPath(Constants.TASK_NODES_PATH + "/" + info.getIp(),
						json.getBytes());
		this.registerIp = ip;
	}

	private void initWorkers(CuratorFramework zkClient) {
		logger.info("启动Workers,一共 {} 个线程",  Constants.DEFAULT_WORKER_NUM);
		int worker_num = Constants.DEFAULT_WORKER_NUM;
		for (int i = 0; i < worker_num; i++) {
			Worker worker = new Worker(zkClient);
			Thread wkt = new Thread(worker);
			wkt.start();
		}
	}

	public String getRealIp() throws SocketException {
		String localip = null;// 本地IP，如果没有配置外网IP则返回它
		String netip = null;// 外网IP

		Enumeration<NetworkInterface> netInterfaces = NetworkInterface
				.getNetworkInterfaces();
		InetAddress ip = null;
		boolean finded = false;// 是否找到外网IP
		while (netInterfaces.hasMoreElements() && !finded) {
			NetworkInterface ni = netInterfaces.nextElement();
			Enumeration<InetAddress> address = ni.getInetAddresses();
			while (address.hasMoreElements()) {
				ip = address.nextElement();
				if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
						&& ip.getHostAddress().indexOf(":") == -1) {// 外网IP
					netip = ip.getHostAddress();
					finded = true;
					break;
				} else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
						&& ip.getHostAddress().indexOf(":") == -1) {// 内网IP
					localip = ip.getHostAddress();
				}
			}
		}

		if (netip != null && !"".equals(netip)) {
			return netip;
		} else {
			return localip;
		}
	}
}
