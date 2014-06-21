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
        ArrayList<Reminder> reminders = getJSONFileContents(context);
        if(reminders == null){
            Log.e(TAG, "Reminder list null, can't throw notification.");
            return;
        }

        // find reminder
        Reminder r = findReminder(intent, reminders);
        if(r == null){
            Log.e(TAG, "Couldn't find reminder. Can't throw notification.");
            return;
        }

        Log.d(TAG,"Setting reminder to completed");
        r.setCompleted(true);
        r.writeToFile(context);
    }


    private ArrayList<Reminder> getJSONFileContents(Context context){
        Log.d(TAG, "Looking for file " + context.getFilesDir() + File.pathSeparator + Reminder.filename);

        // Check for file
        File file = new File(context.getFilesDir(), Reminder.filename);
        if(!file.exists()){
            return null;
        }
        Log.d(TAG,"JSON file found. Loading in reminders.");

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return Reminder.readJsonStream(fileInputStream);
        } catch (Exception e){
            Log.e(TAG, "Error reading existing JSON file: "+e);
        }
        Log.e(TAG, "Could not read input stream to get existing reminders.");
        return null;
    }

    private Reminder findReminder(Intent intent, ArrayList<Reminder> reminders){

        int reminderId = Reminder.BAD_REMINDER_ID;

        // Get reminder ID of reminder
        reminderId = intent.getIntExtra(Reminder.INTENT_REMINDER_ID, reminderId);
        if(reminderId == Reminder.BAD_REMINDER_ID) {
            Log.e(TAG, "Intent data did not contain reminder ID. Cannot throw notification");
            return null;
        }

        Log.d(TAG,"Reminder ID: " + reminderId);

        for(Reminder r : reminders){
            if(r.getReminderID() == reminderId){
                Log.d(TAG, "Found reminder.");
                r.outputReminderToLog();
                return r;
            }
        }

        // if we're here, we don't have a matching reminder
        Log.e(TAG, "Reminder ID not in reminder list. Cannot throw notification");
        return null;
    }
}
