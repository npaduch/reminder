package com.npaduch.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by nolanpaduch on 6/11/14.
 *
 * Class that is called when an alarm goes off.
 * This will alert the user that a reminder is
 * due.
 */
public class AlarmReceiver extends BroadcastReceiver{

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Reminder Due.");
        Toast.makeText(context, "Reminder due!", Toast.LENGTH_SHORT).show();
    }
}
