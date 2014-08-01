package com.npaduch.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
            Log.e(TAG, "Reminder list null, can't set reminder to completed");
            return;
        }

        // find reminder
        Reminder r = Reminder.findReminder(
                intent.getIntExtra(Reminder.INTENT_REMINDER_ID, Reminder.BAD_REMINDER_ID),
                reminders);
        if(r == null){
            Log.e(TAG, "Couldn't find reminder. Can't set reminder to completed");
            return;
        }

        Log.d(TAG,"Setting reminder to completed");
        // check if we need to reschedule
        r.checkRecurrence(context);
        r.writeToFile(context);

        // send event in case activity already running
        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(
                new BusEvent( BusEvent.TYPE_REMOVE, BusEvent.TARGET_PENDING, r));

    }

}
