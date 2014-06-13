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

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Reminder Due.");
        throwNotification(context);

        //vibrateDevice(context);
    }

    public void throwNotification(Context context){

        // Create instance of notification builder
        // TODO: replace with a real notification icon
        Log.d(TAG, "Initializing notification");
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!");
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
        mBuilder.setLights(Color.GREEN, LED_ON_TIME, LED_OFF_TIME);
        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, note);

    }

}
