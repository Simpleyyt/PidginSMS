package com.yimi.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClientSocket {
	private byte[] buffer = new byte[1024];
	private String host;
	private int port;
	private DatagramSocket ds = null;

	public UdpClientSocket(String host, int port) throws Exception {
		ds = new DatagramSocket();
		ds.setSendBufferSize(1024);
		this.host = host;
		this.port = port;
	}

	public UdpClientSocket(int port) throws Exception {
		ds = new DatagramSocket();
		ds.setBroadcast(true);
		ds.setSendBufferSize(1024);
		this.host = "255.255.255.255";
		this.port = port;
	}
	
	public void setIP(String ip) {
		this.host = ip;
	}

	public final void setSoTimeout(final int timeout) throws Exception {
		ds.setSoTimeout(timeout);
	}

	public final int getSoTimeout() throws Exception {
		return ds.getSoTimeout();
	}

	public final DatagramSocket getSocket() {
		return ds;
	}

	public final DatagramPacket send(final byte[] bytes) throws IOException {
		DatagramPacket dp = new DatagramPacket(bytes, bytes.length,
				InetAddress.getByName(host), port);
		ds.send(dp);
		return dp;
	}

	public final String receive(final String lhost, final int lport)
			throws Exception {
		DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
		ds.receive(dp);
		String info = new String(dp.getData(), 0, dp.getLength());
		return info;
	}

	public final void close() {
		try {
			ds.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		int serverPort = 8888;
		UdpClientSocket client = new UdpClientSocket(serverPort);
		client.send(("Hello World!").getBytes());
	}
}