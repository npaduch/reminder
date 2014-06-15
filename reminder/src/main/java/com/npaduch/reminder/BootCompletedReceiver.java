package com.npaduch.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by nolanpaduch on 6/14/14.
 *
 * Receive broadcast for when boot is complete.
 * Re-initialize all alarms
 *
 */

// TODO: Does this need to be done when the app is updated?

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "System rebooted. Reinitialize reminders.");

        ArrayList<Reminder> reminders = getJSONFileContents(context);
        if(reminders == null){
            Log.e(TAG, "Reminder list null, can't re-initialize reminders");
            return;
        }

        setAlarms(context, reminders);
        Log.d(TAG, "BootCompletedReceiver Complete");
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