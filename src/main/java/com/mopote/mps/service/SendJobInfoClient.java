package com.mopote.mps.service;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;

import com.mopote.mps.enums.EJobType;
import com.mopote.mps.enums.EScheduleStatus;
import com.mopote.mps.enums.EScheduleType;
import com.mopote.mps.job.JobInfo;
import com.mopote.mps.utils.ByteObjConverter;

public class SendJobInfoClient {
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	private Bootstrap bootstrap = new Bootstrap(); // (1)

	public SendJobInfoClient() {
		init();
	}

	public void init() {
		bootstrap.group(workerGroup); // (2)
		bootstrap.channel(NioSocketChannel.class); // (3)
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("encoder",
						new MessageToByteEncoder<JobInfo>() {

							@Override
							protected void encode(ChannelHandlerContext ctx,
									JobInfo msg, ByteBuf out) throws Exception {
								byte[] body = ByteObjConverter
										.objectToByte(msg);
								int dataLength = body.length; // 读取消息的长度
								out.writeInt(dataLength); // 先将消息长度写入，也就是消息头
								out.writeBytes(body); // 消息体中包含我们要发送的数据
							}

						});
			}
		});
	}

	public void close() {
		workerGroup.shutdownGracefully();
	}

	public void send(TaskNodeInfo target, JobInfo jobInfo)
			throws InterruptedException {
		Channel ch = bootstrap.connect(target.getIp(), target.getPort()).sync()
				.channel();
		ch.writeAndFlush(jobInfo);
	}

	public static void main(String[] args) throws Exception {
		String host = "localhost";
		int port = 9988;

		SendJobInfoClient client = new SendJobInfoClient();
		client.send(new TaskNodeInfo(host, port, "n", 123), new JobInfo("name",
				EJobType.HIVE, EScheduleType.CRON, EScheduleStatus.ON, "cron"));
		client.send(new TaskNodeInfo(host, port, "n", 123), new JobInfo("name",
				EJobType.HIVE, EScheduleType.CRON, EScheduleStatus.ON, "cron"));
		client.send(new TaskNodeInfo(host, port, "n", 123), new JobInfo("name",
				EJobType.HIVE, EScheduleType.CRON, EScheduleStatus.ON, "cron"));
		
		client.close();
	}
}
