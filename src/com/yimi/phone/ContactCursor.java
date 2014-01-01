package com.yimi.phone;

import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

import com.yimi.phone.Contact.ContactInfo;

public class ContactCursor {

	private Cursor cursor;
	private ContactInfo contact;

	public ContactCursor(Cursor cursor) {
		this.cursor = cursor;
		contact = new ContactInfo();
	}

	public boolean moveToNext() {
		boolean flag;
		String phoneNumber = null;
		String contactName = null;
		while (flag = cursor.moveToNext()) {
			phoneNumber = cursor.getString(Contact.PHONES_NUMBER_INDEX);
			int phoneType = cursor.getInt(Contact.PHONES_TYPE_INDEX);
			if (TextUtils.isEmpty(phoneNumber)
					|| phoneType != Phone.TYPE_MOBILE) {
				continue;
			}
			contactName = cursor.getString(Contact.PHONES_DISPLAY_NAME_INDEX);
			break;
		}
		contact.phone = phoneNumber;
		contact.Name = contactName;
		contact.group = "手机联系人";
		return flag;
	}

	public ContactInfo getContact() {
		return this.contact;
	}
}
