package com.yimi.phone;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

public class MessageSender {
	private Context mContext;

	public MessageSender(Context context) {
		mContext = context;
	}

	public void sendSMS(String phone, String message) {
		PendingIntent pi = PendingIntent.getActivity(mContext, 0, new Intent(
				mContext, MessageSender.class), 0);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phone, null, message, pi, null);
		storeSMS(phone, message);
	}
	
	private void storeSMS(String phone, String message) {
		ContentValues values = new ContentValues();
		values.put("date", System.currentTimeMillis());
		values.put("read", 0);
		values.put("type", 2);
		values.put("address", phone);
		values.put("body", message);
		mContext.getContentResolver().insert(Uri.parse("content://sms/sent"),
				values);
	}

	public static void test(Context context) {
		MessageSender ms = new MessageSender(context);
		ms.sendSMS("10086", "cxgprs");
	}
}
