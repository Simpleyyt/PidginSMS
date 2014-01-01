package com.yimi.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class MessageRev {
	private final String SMS_RECIEVE = "android.provider.Telephony.SMS_RECEIVED";
	private static final String TAG = "MessageRev";
	private SMSRecieveListener listener = null;
	private Context mContext;
	private BroadcastReceiver reciceveMessage = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;
			String phone;
			String message;

			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];
				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					phone = msgs[i].getOriginatingAddress();
					message = msgs[i].getMessageBody();
					if (phone.contains("+86"))
						phone = phone.replace("+86", "");
					if (listener != null)
						listener.OnSMSRecive(phone, message);
				}
			}
		}
	};
	public MessageRev(Context context) {
		mContext = context;
		context.registerReceiver(reciceveMessage, new IntentFilter(SMS_RECIEVE));
	}
	
	public void Close() {
		mContext.unregisterReceiver(reciceveMessage);
	}
	
	public void addSMSRecieveListener(SMSRecieveListener listener) {
		this.listener = listener;
	}
	
	public static interface SMSRecieveListener {
		public void OnSMSRecive(String phone, String msg);
	}
	

	
	public static void test(final Context context) {
		MessageRev mr = new MessageRev(context);
		mr.addSMSRecieveListener(new SMSRecieveListener() {

			@Override
			public void OnSMSRecive(String phone, String msg) {
				// TODO Auto-generated method stub
				Log.i(TAG, String.format("Message from %s: %s", phone, msg));
				Toast.makeText(context, String.format("Message from %s: %s", phone, msg), Toast.LENGTH_LONG).show();
			}
			
		});
	}
}
