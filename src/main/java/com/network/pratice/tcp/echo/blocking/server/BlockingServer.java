package com.network.pratice.tcp.echo.blocking.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 블로킹 서버.
 * 
 * @author seong
 *
 */
public class BlockingServer {
	public static void main(String[] args) throws Exception{
		BlockingServer server = new BlockingServer();
		server.run();
	}
	
	private void run() throws Exception {
		ServerSocket server = new ServerSocket(8888);
		System.out.println("서버 대기중...");
		
		while(true) {
			Socket socket = server.accept();
			System.out.print("클라이언트 연결됨...");
			
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			
			while(true) {
				try {
					int request = in.read();
					out.write(request);
				} catch (Exception e) {
					break;
				}
			}
		}
	}
}
