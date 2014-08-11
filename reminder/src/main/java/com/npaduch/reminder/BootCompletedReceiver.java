package com.npaduch.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by nolanpaduch on 6/14/14.
 *
 * Receive broadcast for when boot is complete.
 * Re-initialize all alarms
 *
 */

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "System rebooted. Reinitialize reminders.");

        ArrayList<Reminder> reminders = Reminder.getJSONFileContents(context);
        if(reminders == null){
            Log.e(TAG, "Reminder list null, can't re-initialize reminders");
            return;
        }

        setAlarms(context, reminders);
        Log.d(TAG, "BootCompletedReceiver Complete");
    }

    private void setAlarms(Context context, ArrayList<Reminder> reminders){
        for(Reminder r : reminders){
            if(!r.isCompleted()) {
                Log.d(TAG, "Setting alarm for: ");
                r.outputReminderToLog();
                r.setAlarm(context);
            }
        }
    }

}