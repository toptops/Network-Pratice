package com.network.pratice.tcp.echo.server;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import com.network.pratice.tcp.echo.server.handler.EchoServerHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@Service
public class EchoServer {

	private final int port = 8888;
	private ChannelFuture closeFuture;

	
	public void start() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		ServerBootstrap bootStrap = new ServerBootstrap();
		try {
			bootStrap.group(bossGroup, workerGroup)
					 .channel(NioServerSocketChannel.class)
					 .childHandler(new ChannelInitializer<SocketChannel>() {
						 @Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new EchoServerHandler());
						}
					});
			ChannelFuture future = bootStrap.bind(port).sync();
			closeFuture = future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	
	@PreDestroy
	public void end() {
		if ( closeFuture == null ) {
			return;
		}

		closeFuture.cancel( true );
	}
}
