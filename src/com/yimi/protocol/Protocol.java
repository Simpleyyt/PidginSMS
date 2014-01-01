package com.yimi.protocol;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.json.JSONStringer;

import com.yimi.phone.Contact.ContactInfo;
import com.yimi.socket.UdpClientSocket;

public class Protocol {

	private Header header;
	private byte[] dist;
	private byte[] key = new byte[16];

	public Protocol(Header header) {
		this.header = header;
	}

	public Protocol(Header header, String user, String pwd) throws Exception {
		this.header = header;
		setAuth(user, pwd);
	}

	public void setAuth(String user, String pwd)
			throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(user.getBytes());
		dist = md.digest();
		md = MessageDigest.getInstance("SHA-1");
		md.update(user.getBytes());
		md.update(pwd.getBytes());
		System.arraycopy(md.digest(), 0, key, 0, 16);
	}

	public byte[] encrypt(byte[] b) throws Exception {
		IvParameterSpec ips = new IvParameterSpec(key);
		Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKey aesKey = new SecretKeySpec(key, "AES");
		encryptCipher.init(Cipher.ENCRYPT_MODE, aesKey, ips);
		return encryptCipher.doFinal(b);
	}

	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	public byte[] decrypt(byte[] b) throws Exception {
		IvParameterSpec ips = new IvParameterSpec(key);
		Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKey aesKey = new SecretKeySpec(key, "AES");
		encryptCipher.init(Cipher.DECRYPT_MODE, aesKey, ips);
		return encryptCipher.doFinal(b);
	}

	public void sendJson(UdpClientSocket socket, JSONObject json)
			throws Exception {
		JSONStringer js = new JSONStringer();
		String str = js.object().key("ver").value(header.ver).key("data")
				.value(json).endObject().toString();
		// System.out.println("Json to send:");
		// System.out.println(str);
		byte[] b = encrypt(str.getBytes());
		socket.send(byteMerger(dist, b));
	}
	
	public void sendKeepAlive(UdpClientSocket socket) throws Exception {
		socket.send(dist);
	}

	public boolean vertify(byte[] source) {
		for (int i = 0; i < dist.length; i++) {
			if (source[i] != dist[i])
				return false;
		}

		return true;
	}

	public void sendAuth(UdpClientSocket socket, boolean isSuccess)
			throws Exception {
		String ret = "success";
		if (!isSuccess)
			ret = "fail";
		JSONObject obj = new JSONObject();
		obj.put("type", "auth");
		obj.put("result", ret);
		sendJson(socket, obj);
	}

	public void sendReq(UdpClientSocket socket, String ip) throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("type", "req");
		obj.put("host", ip);
		sendJson(socket, obj);
	}

	public void sendMsg(UdpClientSocket socket, String who, String msg)
			throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("type", "msg");
		obj.put("who", who);
		obj.put("msg", msg);
		sendJson(socket, obj);
	}
	
	public void sendLogout(UdpClientSocket socket)
			throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("type", "logout");
		sendJson(socket, obj);
	}

	public void sendContact(UdpClientSocket socket, ContactInfo contact)
			throws Exception {
		JSONObject obj = new JSONObject();
		obj.put("type", "contact");
		obj.put("name", contact.Name);
		obj.put("phone", contact.phone);
		obj.put("group", contact.group);
		sendJson(socket, obj);
	}

	public static void printHexString(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			System.out.print(hex.toUpperCase());
			System.out.print(' ');
		}
		System.out.println();
	}

	public static void printDivider() {
		System.out
				.println("===============================================================");
	}

	public static void main(String args[]) throws Exception {
		// test key and dist
		Header header = new Header("1.0");
		Protocol ptl = new Protocol(header, "user", "pwd");
		System.out.println("The key:");
		printHexString(ptl.key);
		System.out.println("The dist:");
		printHexString(ptl.dist);

		printDivider();

		// test padding
		String str = "Hello World!";
		System.out.println("Before padding");
		System.out.println(str);
		System.out.printf("length:%d\n", str.length());

		byte[] b = str.getBytes();
		System.out.println("After padding");
		System.out.println(new String(b));
		System.out.printf("length:%d\n", b.length);

		printDivider();

		// test encrypt
		byte[] encry_b = ptl.encrypt(str.getBytes());
		System.out.println("Before Encrypt:");
		printHexString(str.getBytes());
		System.out.println("Encrypted data:");
		printHexString(encry_b);

		printDivider();

		// test decrypt
		System.out.println("Before decrypt:");
		printHexString(encry_b);
		System.out.println("Decrypted data:");
		byte[] decrypt_b = ptl.decrypt(encry_b);
		printHexString(decrypt_b);
		System.out.println(new String(decrypt_b));

		printDivider();

		// send data
		System.out.println("Send auth");
		UdpClientSocket client = new UdpClientSocket(8888);
		ptl.sendAuth(client, true);
	}
}
