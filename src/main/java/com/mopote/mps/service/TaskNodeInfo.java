package com.mopote.mps.service;

public class TaskNodeInfo {
	private String ip;
	private int port;
	private String name;
	private long register_time;

	
	public TaskNodeInfo() {
		super();
	}

	public TaskNodeInfo(String ip, int port, String name, long register_time) {
		super();
		this.ip = ip;
		this.port = port;
		this.name = name;
		this.register_time = register_time;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getRegister_time() {
		return register_time;
	}

	public void setRegister_time(long register_time) {
		this.register_time = register_time;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
