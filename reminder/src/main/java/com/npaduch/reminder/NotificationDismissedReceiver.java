package com.npaduch.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by nolanpaduch on 6/20/14.
 *
 * Receiver for when a reminder is swiped away
 */
public class NotificationDismissedReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationDissmissed";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Reminder dismissed");

        // Load in reminders
        ArrayList<Reminder> reminders = Reminder.getJSONFileContents(context);
        if(reminders == null){
            Log.e(TAG, "Reminder list null, can't throw notification.");
            return;
        }

        // find reminder
        Reminder r = Reminder.findReminder(
                intent.getIntExtra(Reminder.INTENT_REMINDER_ID, Reminder.BAD_REMINDER_ID),
                reminders);
        if(r == null){
            Log.e(TAG, "Couldn't find reminder. Can't throw notification.");
            return;
        }

        Log.d(TAG,"Setting reminder to completed");
        r.setCompleted(true);
        r.writeToFile(context);
    }

}