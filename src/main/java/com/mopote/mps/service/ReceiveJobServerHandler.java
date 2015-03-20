package com.mopote.mps.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mopote.mps.job.JobInfo;

public class ReceiveJobServerHandler extends
		SimpleChannelInboundHandler<JobInfo> {
	private static Logger logger = LoggerFactory
			.getLogger(ReceiveJobServerHandler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, JobInfo jobInfo)
			throws Exception {
		logger.info("收到一个job : {}",jobInfo.getName());
		Worker.addJob(jobInfo);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		logger.info("New client connected");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		logger.info("Client closed ");
	}

}
