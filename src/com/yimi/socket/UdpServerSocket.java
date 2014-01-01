package com.yimi.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UdpServerSocket {
	private byte[] buffer = new byte[2024];
	private DatagramSocket ds = null;
	private DatagramPacket packet = null;
	private String orgIp;

	public UdpServerSocket(int port) throws Exception {
		ds = new DatagramSocket(port);
		System.out.println("Service start!");
	}

	public final String getOrgIp() {
		return orgIp;
	}

	public final void setSoTimeout(int timeout) throws Exception {
		ds.setSoTimeout(timeout);
	}

	public final int getSoTimeout() throws Exception {
		return ds.getSoTimeout();
	}

	public final byte[] receive() throws IOException {
		packet = new DatagramPacket(buffer, buffer.length);
		ds.receive(packet);
		orgIp = packet.getAddress().getHostAddress();
		byte[] b = new byte[packet.getLength()];
		System.arraycopy(packet.getData(), 0, b, 0, packet.getLength());
		return b;
	}

	public final void response(String info) throws IOException {
		/*
		System.out.println("Client IP : "
				+ packet.getAddress().getHostAddress() + ",Port:"
				+ packet.getPort());
				*/
		DatagramPacket dp = new DatagramPacket(buffer, buffer.length,
				packet.getAddress(), packet.getPort());
		dp.setData(info.getBytes());
		ds.send(dp);
	}

	public final void setLength(int bufsize) {
		packet.setLength(bufsize);
	}

	public final InetAddress getResponseAddress() {
		return packet.getAddress();
	}

	public final int getResponsePort() {
		return packet.getPort();
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
		UdpServerSocket udpServerSocket = new UdpServerSocket(serverPort);
		while (true) {
			byte[] b = udpServerSocket.receive();
			System.out.println(new String(b));
		}
	}
}