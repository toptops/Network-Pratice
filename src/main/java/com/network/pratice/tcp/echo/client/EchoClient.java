package com.network.pratice.tcp.echo.client;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import com.network.pratice.tcp.echo.client.handler.EchoClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * EchoClient 서비스
 * EchoServer와 마찬가지로 별다른 기능은 없다.
 * 서버 접속 후 Active시 메세지를 날린 후 출력, 서버에서 메시지를 받으면 출력 후 종료
 * 
 * @author seong
 *
 */
@Service
public class EchoClient {
	private final String ip = "127.0.0.1";
	private final int port = 8888;
	
	private ChannelFuture closeFuture;

	public void start() {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		
		try {
			bootstrap.group(group)
					 .channel(NioSocketChannel.class)
					 .handler(new ChannelInitializer<SocketChannel>() {
						 @Override
						protected void initChannel(SocketChannel ch) throws Exception {
							 ChannelPipeline p = ch.pipeline();
							 p.addLast(new EchoClientHandler());
						}
					});
			
			ChannelFuture future = bootstrap.connect(ip, port).sync();
			closeFuture = future.channel().closeFuture().sync();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		} finally {
			group.shutdownGracefully();
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
