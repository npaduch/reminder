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


    // length to flash LED  in ms
    private static final int LED_ON_TIME = 3000;
    private static final int LED_OFF_TIME = 3000;
    private static final int LED_COLOR = Color.CYAN;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Reminder Due.");

        ArrayList<Reminder> reminders = Reminder.getJSONFileContents(context);
        if(reminders == null){
            Log.e(TAG, "Reminder list null, can't throw notification.");
            return;
        }

        Reminder r = Reminder.findReminder(
                intent.getIntExtra(Reminder.INTENT_REMINDER_ID, Reminder.BAD_REMINDER_ID), reminders);
        if(r == null){
            Log.e(TAG, "Couldn't find reminder. Can't throw notification.");
            return;
        }

        throwNotification(context, r);
    }

    void throwNotification(Context context, Reminder r){

        // Create instance of notification builder
        // TODO: replace with a real notification icon
        Log.d(TAG, "Initializing notification");
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(r.getDescription())
                    .setContentText(r.getDateTimeString(context))
                    .setDeleteIntent(createOnDismissedIntent(context, r.getReminderID()))
                    .setAutoCancel(true);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);
        // add remidner info
        resultIntent.putExtra(Reminder.INTENT_REMINDER_ID, r.getReminderID());

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

        // create intents for actionable buttons (snooze)
        // TODO: change these to spawn service so we don't launch app
        Intent snoozeStaticIntent = new Intent(context, ReminderNotificationService.class);
        snoozeStaticIntent.putExtra(Reminder.INTENT_REMINDER_ID, r.getReminderID());
        snoozeStaticIntent.setAction(MainActivity.ACTION_SNOOZE_STATIC);
        PendingIntent piSnoozeStatic = PendingIntent.getService(
                context,
                0,
                snoozeStaticIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent snoozeCustomIntent = new Intent(context, MainActivity.class);
        snoozeCustomIntent.putExtra(Reminder.INTENT_REMINDER_ID, r.getReminderID());
        snoozeCustomIntent.setAction(MainActivity.ACTION_SNOOZE_CUSTOM);
        PendingIntent piSnoozeCustom = PendingIntent.getActivity(
                context,
                0,
                snoozeCustomIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        SettingsHandler settingsHandler = new SettingsHandler();
        StringBuilder sb = new StringBuilder();
        sb.append(settingsHandler.getSnoozeString(context));
        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(r.getDateTimeString(context)))
                .addAction (R.drawable.ic_action_alarms,
                        sb.toString(), piSnoozeStatic)
                .addAction(R.drawable.ic_action_alarms,
                        context.getResources().getString(R.string.notification_snooze_custom), piSnoozeCustom);

        // set default alert tones/lights/vibrations
        mBuilder.setLights(LED_COLOR, LED_ON_TIME, LED_OFF_TIME);
        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(r.getReminderID(), note);

    }

    private PendingIntent createOnDismissedIntent(Context context, int reminderID) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra(Reminder.INTENT_REMINDER_ID, reminderID);

        return PendingIntent.getBroadcast(context.getApplicationContext(),
                        reminderID, intent, 0);
    }


}
