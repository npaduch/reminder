package com.npaduch.reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by nolanpaduch on 6/11/14.
 *
 * Class that is called when an alarm goes off.
 * This will alert the user that a reminder is
 * due.
 */
public class AlarmReceiver extends BroadcastReceiver{

    private static final String TAG = "AlarmReceiver";

    // Random ID for notification
    private static final int mId = 10492842;

    // length to flash LED  in ms
    private static final int LED_ON_TIME = 3000;
    private static final int LED_OFF_TIME = 3000;
    private static final int LED_COLOR = Color.CYAN;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Reminder Due.");

        ArrayList<Reminder> reminders = getJSONFileContents(context);
        if(reminders == null){
            Log.e(TAG, "Reminder list null, can't throw notification.");
            return;
        }

        Reminder r = findReminder(intent, reminders);
        if(r == null){
            Log.e(TAG, "Couldn't find reminder. Can't throw notification.");
            return;
        }

        throwNotification(context, r);

        //vibrateDevice(context);
    }

    public void throwNotification(Context context, Reminder r){

        // Create instance of notification builder
        // TODO: replace with a real notification icon
        Log.d(TAG, "Initializing notification");
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(r.getDescription())
                    .setContentText(r.getDateTimeString())
                    .setDeleteIntent(createOnDismissedIntent(context, r.getReminderID()));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        // set default alert tones/lights/vibrations
        mBuilder.setLights(LED_COLOR, LED_ON_TIME, LED_OFF_TIME);
        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, note);

        // TODO: move this of the handling
        r.setCompleted(true);

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

    private PendingIntent createOnDismissedIntent(Context context, int reminderID) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra(Reminder.INTENT_REMINDER_ID, reminderID);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),
                        reminderID, intent, 0);
        return pendingIntent;
    }

}
