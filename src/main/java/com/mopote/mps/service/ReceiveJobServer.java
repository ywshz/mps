package com.mopote.mps.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import com.mopote.mps.utils.ByteObjConverter;
import com.mopote.mps.utils.Constants;

public class ReceiveJobServer {

	public void start() throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					// 字符串解码 和 编码
					pipeline.addLast("decoder", new ByteToMessageDecoder() {

						@Override
						protected void decode(ChannelHandlerContext ctx,
								ByteBuf in, List<Object> out) throws Exception {
							if (in.readableBytes() < 4) { // int类型长度
								return;
							}
							in.markReaderIndex(); // 我们标记一下当前的readIndex的位置
							int dataLength = in.readInt(); // 读取传送过来的消息的长度。ByteBuf
															// 的readInt()方法会让他的readIndex增加4
							if (dataLength < 0) { // 我们读到的消息体长度为0，这是不应该出现的情况，这里出现这情况，关闭连接。
								ctx.close();
							}

							if (in.readableBytes() < dataLength) { // 读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex.
																	// 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
								in.resetReaderIndex();
								return;
							}

							byte[] body = new byte[dataLength]; // 嗯，这时候，我们读到的长度，满足我们的要求了，把传送过来的数据，取出来吧~~
							in.readBytes(body); //
							Object o = ByteObjConverter.byteToObject(body); // 将byte数据转化为我们需要的对象。伪代码，用什么序列化，自行选择
							out.add(o);
						}

					});

					// 自己的逻辑Handler
					pipeline.addLast("handler", new ReceiveJobServerHandler());
				}
			});

			// 服务器绑定端口监听
			ChannelFuture f = b.bind(Constants.JOB_MONITOR_PORT).sync();
			// 监听服务器关闭监听
			f.channel().closeFuture().sync();

			// 可以简写为
			/* b.bind(portNumber).sync().channel().closeFuture().sync(); */
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		new ReceiveJobServer().start();
	}
}
