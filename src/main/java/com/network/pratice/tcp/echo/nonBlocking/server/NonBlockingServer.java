package com.network.pratice.tcp.echo.nonBlocking.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Seletor를 통한 논블로킹 서버..
 * 진짜 코드를 짤때마다 신기하다.
 * 
 * @author seong
 *
 */
public class NonBlockingServer {
	private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<SocketChannel, List<byte[]>>();
	private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);
	
	private void startEchoServer() {
		try (Selector selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();){
			if((serverSocketChannel.isOpen()) && (selector.isOpen())) {
				serverSocketChannel.configureBlocking(false);
				serverSocketChannel.bind(new InetSocketAddress(8888));
				
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
				System.out.println("접속 대기중...");
				
				while(true) {
					selector.select();
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
					
					//들어온 순서대로 이벤트 처리
					while(keys.hasNext()) {
						SelectionKey key = (SelectionKey) keys.next();
						keys.remove();
						
						if(!key.isValid()) {
							continue;
						}
						
						if(key.isAcceptable()) {
							this.acceptOP(key, selector);
						} else if(key.isReadable()) {
							this.readOP(key);
						} else if(key.isWritable()) {
							this.writeOP(key);
						} else {
							System.out.println("서버 소캣을 생성하지 못했습니다.");
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	} 
	
	// 소켓 접속 이벤트
	private void acceptOP(SelectionKey key, Selector selector) throws IOException{
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = serverChannel.accept();
		socketChannel.configureBlocking(false);
		
		System.out.println("클라이언트 연결됨 : " + socketChannel.getRemoteAddress());
		
		// 채널별 쓰기전용 데이터 할당 
		keepDataTrack.put(socketChannel, new ArrayList<byte[]>());
		//Selector에 Read이벤트 등록
		socketChannel.register(selector, SelectionKey.OP_READ);
	}
	
	// 읽기 이벤트
	private void readOP(SelectionKey key) {
		try {
			SocketChannel socketChannel = (SocketChannel) key.channel();
			buffer.clear();
			int numRead = -1;
			try {
				numRead = socketChannel.read(buffer);
			} catch (IOException e) {
				System.out.println("데이터 읽기 에러");
			}
			
			if(numRead == -1) {
				this.keepDataTrack.remove(socketChannel);
				System.out.println("클라이언트 연결 종료 : " + socketChannel.getRemoteAddress());
				socketChannel.close();
				key.cancel();
				return;
			} 
			
			byte[] data = new byte[numRead];
			System.arraycopy(buffer.array(), 0, data, 0, numRead);
			System.out.println(new String(data, "UTF-8") + " from " + socketChannel.getRemoteAddress());
			
			doEchoJob(key, data);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	// 쓰기 이벤트
	private void writeOP(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		
		List<byte[]> channelData = keepDataTrack.get(socketChannel);
		Iterator<byte[]> its = channelData.iterator();
		
		while(its.hasNext()) {
			byte[] it = its.next();
			its.remove();
			socketChannel.write(ByteBuffer.wrap(it));
		}
		
		//이벤트 읽기 등록
		key.interestOps(SelectionKey.OP_READ);
	}
	
	// 읽기 이벤트 서브 함수 
	private void doEchoJob(SelectionKey key, byte[] data) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		List<byte[]> channelData = keepDataTrack.get(socketChannel);
		channelData.add(data);
		
		// 이벤트 읽기 등록
		key.interestOps(SelectionKey.OP_WRITE);
	}
	
	public static void main(String[] args) {
		NonBlockingServer main = new NonBlockingServer();
		main.startEchoServer();
	}
}
