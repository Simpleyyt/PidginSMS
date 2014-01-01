package com.yimi.phone;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

public class Contact {
	private static final String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, Phone.NUMBER, Phone.TYPE };
	public static final int PHONES_DISPLAY_NAME_INDEX = 0;
	public static final int PHONES_NUMBER_INDEX = 1;
	public static final int PHONES_TYPE_INDEX = 2;
	public static final String TAG = "contact";

	private Cursor phoneCursor;
	private ContentResolver resolver;
	private Context mContext = null;

	public Contact(Context context) {
		mContext = context;
		resolver = mContext.getContentResolver();
		phoneCursor = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION,
				null, null, null);
	}

	public ContactCursor query() {
		ContentResolver resolver = mContext.getContentResolver();
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				PHONES_PROJECTION, null, null, null);
		if (phoneCursor == null)
			return null;
		return new ContactCursor(phoneCursor);
	}

	public static class ContactInfo {
		public String Name;
		public String phone;
		public String group;
	}
	
	public static void test(Context ctx) {
		Contact c = new Contact(ctx);
		ContactCursor cc = c.query();
		if (cc == null) {
			Log.e(TAG, "Can't get cursor");
			return;
		}
		while (cc.moveToNext()) {
			Log.e(TAG, String.format("Contact: %s %s %s",cc.getContact().Name, cc.getContact().phone, cc.getContact().group));
		}
	}
}
