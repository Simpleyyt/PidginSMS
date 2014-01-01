package com.yimi.socket;

import java.util.EventListener;
import com.yimi.protocol.*;

public class UdpListenThread implements Runnable {
	private UdpServerSocket socket;
	private UdpListener listener;
	private Thread thread;

	public UdpListenThread(int port) {
		try {
			socket = new UdpServerSocket(port);
			thread = new Thread(this);
		} catch (Exception e) {
			if (listener != null) {
				listener.Error(this, e);
			}
		}
	}

	public void addListener(UdpListener lis) {
		this.listener = lis;
	}

	public void beginListen() {
		thread.start();
	}

	public void stopListen() {
		thread.stop();
		socket.close();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (true) {
				byte[] b = socket.receive();
				if (listener != null) {
					listener.MessageCome(this, b);
				}
			}
		} catch (Exception e) {
			if (listener != null) {
				listener.Error(this, e);
			}
		}
	}

	public interface UdpListener extends EventListener {
		public abstract void Error(UdpListenThread sock, Exception e);

		public abstract void MessageCome(UdpListenThread sock, byte[] msg);
	};

	public static void main(String[] args) throws Exception {
		UdpListenThread sock = new UdpListenThread(8888);
		sock.addListener(new UdpListenThread.UdpListener() {
			@Override
			public void Error(UdpListenThread sock, Exception e) {
				e.printStackTrace();
			}

			@Override
			public void MessageCome(UdpListenThread sock, byte[] msg) {
				System.out.println("Message recieve");
				String str = new String(msg);
				System.out.println(str);
			}
		});
		sock.beginListen();
		System.out.println("listened");
		/*
		try {
			while (true)
				Thread.sleep(15000);
		} catch (InterruptedException e) {
			System.out.println("got interrupted!");
		}
		System.out.println("Exit");
		*/
	}

}
