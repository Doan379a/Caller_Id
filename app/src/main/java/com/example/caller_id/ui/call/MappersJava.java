package com.example.caller_id.ui.call;

import static android.telecom.Call.STATE_ACTIVE;
import static android.telecom.Call.STATE_CONNECTING;
import static android.telecom.Call.STATE_DIALING;
import static android.telecom.Call.STATE_DISCONNECTED;
import static android.telecom.Call.STATE_RINGING;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telecom.Call;

import org.jetbrains.annotations.NotNull;

public class MappersJava {
    @TargetApi(Build.VERSION_CODES.M)
    public static GsmCall toGsmCall(@NotNull Context context, @NotNull Call call) {
        GsmCall.Status status = toGsmCallStatus(call.getState());
        Uri handle = call.getDetails().getHandle();
        String phoneNumber = handle.getSchemeSpecificPart();
        String displayName = getContactName(context, phoneNumber);
        return new GsmCall(status, displayName, phoneNumber);
    }

    private static final GsmCall.Status toGsmCallStatus(int callState) {
        GsmCall.Status gsmCallState;
        switch(callState) {
            case STATE_DIALING:
                gsmCallState = GsmCall.Status.DIALING;
                break;
            case STATE_RINGING:
                gsmCallState = GsmCall.Status.RINGING;
                break;
            case STATE_ACTIVE:
                gsmCallState = GsmCall.Status.ACTIVE;
                break;
            case STATE_DISCONNECTED:
                gsmCallState = GsmCall.Status.DISCONNECTED;
                break;
            case STATE_CONNECTING:
                gsmCallState = GsmCall.Status.CONNECTING;
                break;
            default:
                gsmCallState = GsmCall.Status.UNKNOWN;
                break;
        }

        return gsmCallState;
    }
    private static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                null, null, null);

        if (cursor != null) {
            String name = null;
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
            return name;
        }
        return null;
    }

}
