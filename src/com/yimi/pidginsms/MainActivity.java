package com.yimi.pidginsms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.snail.yimi.R;
import com.yimi.pidginsms.BackgroundService.LocalBinder;

public class MainActivity extends PreferenceActivity {
	private Intent backgroundService;
	private final String TAG = "yimi";
	NotificationManager m_NotificationManager;
	Intent m_Intent;
	BackgroundService service;
	PendingIntent m_PendingIntent;
	Notification m_Notification;
	CheckBoxPreference servicePreference;
	Preference refreshContactsPreference;
	Preference aboutPreference;
	SharedPreferences settings;
	LocalBinder binder;
	boolean isbinded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		m_NotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		backgroundService = new Intent(MainActivity.this,
				BackgroundService.class);
		servicePreference = (CheckBoxPreference) findPreference("pre_yimi_service");
		refreshContactsPreference = (Preference) findPreference("pre_refresh_contacts");
		aboutPreference = (Preference) findPreference("pre_about");
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		servicePreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						// TODO Auto-generated method stub
						CheckBoxPreference pre = (CheckBoxPreference) arg0;
						if (pre.isChecked()) {
							beginService();
						} else {
							stopService();
						}
						return false;
					}

				});
		refreshContactsPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						// TODO Auto-generated method stub
						Toast.makeText(MainActivity.this, "联系人已经发送.",
								Toast.LENGTH_SHORT).show();
						binder.sendContacts();
						return false;
					}

				});
		aboutPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						// TODO Auto-generated method stub

						new AlertDialog.Builder(MainActivity.this)
								.setIcon(R.drawable.ic_launcher)
								.setTitle("Pidgin短信 v1.0")
								.setMessage(
										"作者：Snail\n微博：摇一摇SimpleLife\n邮箱：simpleyyt@gmail.com")
								.setPositiveButton("确定", null).create().show();
						return false;
					}

				});
		if (!isbinded) {
			bindService(backgroundService, mConnection, 0);
			isbinded = true;
		}
	}

	private void stopService() {
		if (backgroundService != null) {
			if (isbinded) {
				unbindService(mConnection);
				isbinded = false;
			}
			stopService(backgroundService);
		}
	}

	private void beginService() {
		String host = getLocalIpAddress();
		String user = settings.getString("pre_user", "user");
		String pwd = settings.getString("pre_pass", "pwd");
		if (host == null) {
			Log.e(TAG, "Unable to get ip");
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putString("user", user);
		bundle.putString("pwd", pwd);
		bundle.putString("host", host);
		backgroundService.putExtras(bundle);
		startService(backgroundService);
		if (!isbinded) {
			bindService(backgroundService, mConnection, 0);
			isbinded = true;
		}
	}

	private String getLocalIpAddress() {
		WifiManager wifiManager = (WifiManager) this
				.getSystemService(android.content.Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		if (ipAddress == 0)
			return null;
		return String.format("%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			binder = (LocalBinder) service;
			isbinded = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			isbinded = false;
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (isbinded) {
			unbindService(mConnection);
			isbinded = false;
		}
		super.onDestroy();
	}

}
