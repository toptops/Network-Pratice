package com.network.pratice.tcp.echo.server.handler;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoServerHandler extends ChannelInboundHandlerAdapter{
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());
		
		System.out.println("수신한 문자열 [" + readMessage + "]");
		
		ctx.write(msg);
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.flush();
	}
}
