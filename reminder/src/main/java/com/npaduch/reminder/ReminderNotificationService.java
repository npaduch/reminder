package com.npaduch.reminder;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by nolanpaduch on 10/25/14.
 *
 * Service to handle notification actions
 * so that the app does not need to be started
 */
public class ReminderNotificationService extends IntentService {

    private static String TAG = "ReminderNotificationService";

    public ReminderNotificationService() {
        super("ReminderNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // get reminder ID
        int reminderId = intent.getIntExtra(
                Reminder.INTENT_REMINDER_ID,
                Reminder.BAD_REMINDER_ID);
        if(reminderId == Reminder.BAD_REMINDER_ID){
            Log.d(TAG, "Reminder ID not in intent. THIS SHOULDN'T HAPPEN");
            // this wasn't called started from a notification
            return;
        }

        // Load in reminders
        ArrayList<Reminder> reminders = Reminder.getJSONFileContents(this);
        if(reminders == null){
            Log.e(TAG, "Reminder list null, can't set reminder to completed");
            return;
        }

        // find reminder
        Reminder r = Reminder.findReminder(
                reminderId,
                reminders);
        if(r == null){
            Log.e(TAG, "Couldn't find reminder. Can't set reminder to completed");
            return;
        }
        Log.d(TAG, "Found reminder");

        Log.d(TAG, "Snoozing reminder for default snooze time...");
        Calendar cal = Calendar.getInstance();
        SettingsHandler settingsHandler = new SettingsHandler();
        r.setMsTime(cal.getTimeInMillis()+settingsHandler.getCustomReminderSnooze(this));
        r.setAlarm(this);
        r.cancelNotification(this);

        Log.d(TAG,"Write back to file after snooze");
        r.writeToFile(this);

        // send event in case activity already running
        BusEvent be = new BusEvent(BusEvent.TYPE_REFRESH_REMINDERS, BusEvent.TARGET_PENDING);
        be.addTarget(BusEvent.TARGET_COMPLETED);
        BusProvider.getInstance().post(be);
    }

}
