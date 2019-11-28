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

/**
 * EchoServer 서비스
 * 별다른 기능은 없고 접속한 클라이언트가 날린 메시지를 그대로 전송 및 출력
 * 
 * @author seong
 *
 */
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
