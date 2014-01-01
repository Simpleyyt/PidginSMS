package com.yimi.pidginsms;

import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.snail.yimi.R;
import com.yimi.phone.Contact;
import com.yimi.phone.ContactCursor;
import com.yimi.phone.MessageRev;
import com.yimi.phone.MessageRev.SMSRecieveListener;
import com.yimi.phone.MessageSender;
import com.yimi.protocol.Header;
import com.yimi.protocol.Protocol;
import com.yimi.socket.UdpClientSocket;
import com.yimi.socket.UdpServerSocket;

public class BackgroundService extends IntentService {
	private UdpServerSocket serverSocket;
	private UdpClientSocket clientSocket;
	private Header header;
	private Protocol ptl;
	private Handler sendReqHandler;
	private Handler keepaliveHandler;
	private final String TAG = "service";
	private String host;
	private String user;
	private String pwd;
	private boolean isLogin = false;
	private MessageSender ms = new MessageSender(this);
	private LocalBinder mBinder = new LocalBinder();
	private boolean isOn = true;

	Intent m_Intent;
	PendingIntent m_PendingIntent;
	Notification m_Notification;
	NotificationManager m_NotificationManager;

	private MessageRev mr;

	private Runnable sendReqRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 在此处添加执行的代码
			new Thread() {
				@Override
				public void run() {
					try {
						ptl.sendReq(clientSocket, host);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();
			sendReqHandler.postDelayed(this, 3000);
		}
	};

	private Runnable keepaliveRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			sendReqHandler.postDelayed(sendReqRunnable, 3000);
		}
	};

	public BackgroundService() {
		super("BackgroudService");
		Log.i(TAG, "Enter Service");
		try {
			serverSocket = new UdpServerSocket(8888);
			clientSocket = new UdpClientSocket(8889);
			keepaliveHandler = new Handler();
			sendReqHandler = new Handler();
			header = new Header("1.0");
			ptl = new Protocol(header);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			HandleError(e);
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		mr = new MessageRev(this);
		m_NotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		showNotification();
		super.onCreate();
	}

	private void HandleError(Exception e) {
		e.printStackTrace();
	}

	private void Process(byte[] b) throws Exception {
		Log.i(TAG, "Message Come");
		if (ptl.vertify(b)) {
			System.out.println("Vertify OK");
			keepaliveHandler.removeCallbacks(keepaliveRunnable);
			keepaliveHandler.postDelayed(keepaliveRunnable, 61000);
			if (b.length == 20) {
				ptl.sendKeepAlive(clientSocket);
				return;
			}
			byte[] data = new byte[b.length - 20];
			System.arraycopy(b, 20, data, 0, b.length - 20);
			byte[] msg = ptl.decrypt(data);
			System.out.println("The msg recieve");
			System.out.println(new String(msg));
			JSONObject jsonobj = new JSONObject(new String(msg));
			JSONObject dataObj = jsonobj.getJSONObject("data");
			dispatcher(dataObj);
		} else {
			System.out.println("Vertify fail");
		}
	}

	private void dispatcher(JSONObject dataObj) throws Exception {
		String type = dataObj.getString("type");
		if (type.equals("login"))
			processLogin(dataObj);
		if (type.equals("reqcontact"))
			processReqContact(dataObj);
		if (type.equals("msg"))
			processMsg(dataObj);
		if (type.equals("logout"))
			processLogout(dataObj);
	}

	private void processLogin(JSONObject obj) throws Exception {
		System.out.println("Enter login");
		isLogin = true;
		sendReqHandler.removeCallbacks(sendReqRunnable);
	}

	private void processLogout(JSONObject obj) throws Exception {
		System.out.println("Enter logout");
		sendReqHandler.postDelayed(sendReqRunnable, 3000);
		keepaliveHandler.removeCallbacks(keepaliveRunnable);
	}

	private void processMsg(JSONObject obj) throws Exception {
		String who = obj.getString("who");
		String msg = obj.getString("msg");
		ms.sendSMS(who, msg);
	}

	private void processReqContact(JSONObject obj) throws Exception {
		System.out.println("Enter contact");
		Contact c = new Contact(this);
		ContactCursor cc = c.query();
		int i = 0;
		if (cc == null) {
			Log.e(TAG, "Can't get cursor");
			return;
		}
		while (cc.moveToNext()) {
			i++;
			ptl.sendContact(clientSocket, cc.getContact());
			Thread.sleep(100);
		}
		System.out.print("The total number: ");
		System.out.println(i);
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Enter Thread");
		mr.addSMSRecieveListener(new SMSRecieveListener() {

			@Override
			public void OnSMSRecive(String phone, String msg) {
				// TODO Auto-generated method stub
				try {
					ptl.sendMsg(clientSocket, phone, msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
		try {
			Bundle bundle = arg0.getExtras();
			user = bundle.getString("user");
			pwd = bundle.getString("pwd");
			host = bundle.getString("host");
			ptl.setAuth(user, pwd);
			sendReqHandler.postDelayed(sendReqRunnable, 3000);
			isOn = true;
			while (isOn) {
				try {
					byte[] b = serverSocket.receive();
					Process(b);
				} catch (Exception e) {
					HandleError(e);
				}
			}
		} catch (Exception e) {
			HandleError(e);
		}
	}

	private void showNotification() {
		m_PendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				MainActivity.class), 0);
		m_Notification = new Notification();
		m_Notification.icon = R.drawable.ic_launcher;
		m_Notification.tickerText = "Pidgin短信服务开启成功";
		m_Notification.defaults = Notification.DEFAULT_SOUND;
		m_Notification.setLatestEventInfo(this, "Pidgin短信服务", "Pidgin短信服务正在运行",
				m_PendingIntent);
		m_Notification.flags = Notification.FLAG_NO_CLEAR;
		m_NotificationManager.notify(0, m_Notification);
	}

	private void cancelNotification() {
		m_NotificationManager.cancelAll();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		isOn = false;
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				try {
					ptl.sendLogout(clientSocket);
					clientSocket.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		cancelNotification();
		mr.Close();
		sendReqHandler.removeCallbacks(sendReqRunnable);
		keepaliveHandler.removeCallbacks(keepaliveRunnable);
		serverSocket.close();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public void sendContacts() {
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						processReqContact(null);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}).start();
		}
	}
}
