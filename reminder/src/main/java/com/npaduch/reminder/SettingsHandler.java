package com.npaduch.reminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by nolanpaduch on 8/12/14.
 *
 * This class will handle getting and saving preferences
 */
public class SettingsHandler {

    private final static String TAG = "SettingsHandler";

    /** KEYS **/
    private final static String nextId = "Next_ID";
    private final static String timeMorning = "time_morning";
    private final static String timeNoon = "time_noon";
    private final static String timeAfternoon = "time_afternoon";
    private final static String timeEvening = "time_evening";
    private final static String timeNight = "time_night";
    private final static String appLaunchCounter = "app_launch_counter";
    private final static String customReminderSnooze = "custom_reminder_snooze";

    /** Default Values **/
    private final static int timeDefaultMorning = 9;
    private final static int timeDefaultNoon = 12;
    private final static int timeDefaultAfternoon = 12 + 3;
    private final static int timeDefaultEvening = 12 + 5;
    private final static int timeDefaultNight = 12 + 8;
    private final static int appLaunchDefaultCounter = 0;
    private final static int defaultCustomReminderSnooze = 15 * 60 * 1000; // 15 minutes

    private final static int badId = -1;
    private final static int badTime = -1;
    private final static int badCounter = -1;

    public SettingsHandler(){};

    public int getNextId(Context context){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(nextId)){
            editor.putInt(nextId, 0); // value to store
            editor.commit();
        }

        int currentID = sp.getInt(nextId, badId);
        if(currentID == badId){
            Log.e(TAG, "Did not return the current Id");
        }

        // increment ID for next time
        editor.putInt(nextId, currentID+1); // value to store
        editor.commit();

        return currentID;
    }

    public long getTimeMorning(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(timeMorning)){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, timeDefaultMorning);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            editor.putLong(timeMorning, cal.getTimeInMillis()); // value to store
            editor.commit();
        }
        return sp.getLong(timeMorning, badTime);
    }

    public long getTimeNoon(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(timeNoon)){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, timeDefaultNoon);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            editor.putLong(timeNoon, cal.getTimeInMillis()); // value to store
            editor.commit();
        }
        return sp.getLong(timeNoon, badTime);
    }

    public long getTimeAfternoon(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(timeAfternoon)){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, timeDefaultAfternoon);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            editor.putLong(timeAfternoon, cal.getTimeInMillis()); // value to store
            editor.commit();
        }
        return sp.getLong(timeAfternoon, badTime);
    }

    public long getTimeEvening(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(timeEvening)){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, timeDefaultEvening);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            editor.putLong(timeEvening, cal.getTimeInMillis()); // value to store
            editor.commit();
        }
        return sp.getLong(timeEvening, badTime);
    }

    public long getTimeNight(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(timeNight)){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, timeDefaultNight);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            editor.putLong(timeNight, cal.getTimeInMillis()); // value to store
            editor.commit();
        }
        return sp.getLong(timeNight, badTime);
    }

    public void setTimeMorning(Context context, long val){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(timeMorning, val); // value to store
        editor.commit();
    }

    public void setTimeNoon(Context context, long val){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(timeNoon, val); // value to store
        editor.commit();
    }

    public void setTimeAfternoon(Context context, long val){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(timeAfternoon, val); // value to store
        editor.commit();
    }

    public void setTimeEvening(Context context, long val){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(timeEvening, val); // value to store
        editor.commit();
    }

    public void setTimeNight(Context context, long val){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(timeNight, val); // value to store
        editor.commit();
    }

    public long getAppLaunchCounter(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where counter has never been set
        if(!sp.contains(appLaunchCounter)){
            editor.putLong(appLaunchCounter, appLaunchDefaultCounter); // value to store
            editor.commit();
        }
        return sp.getLong(appLaunchCounter, badCounter);
    }

    public void incrementAppLaunchCounter(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        long counter = sp.getLong(appLaunchCounter, badCounter);

        editor.putLong(appLaunchCounter, counter+1); // value to store
        editor.commit();
    }

    public long getCustomReminderSnooze(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(customReminderSnooze)){
            editor.putLong(customReminderSnooze, defaultCustomReminderSnooze); // value to store
            editor.commit();
        }
        return sp.getLong(customReminderSnooze, badTime);
    }

    public void setCustomReminderSnooze(Context context, long snoozeTime){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(customReminderSnooze, snoozeTime); // value to store
        editor.commit();
    }

    public String getSnoozeString(Context context){
        long msTime = getCustomReminderSnooze(context);
        StringBuilder sb = new StringBuilder();

        long hours = TimeUnit.MILLISECONDS.toHours(msTime);
        msTime -= TimeUnit.HOURS.toMillis(hours);
        long min = TimeUnit.MILLISECONDS.toMinutes(msTime);
        msTime -= TimeUnit.MINUTES.toMillis(min);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(msTime);

        if(hours > 0){
            sb.append(hours);
            sb.append(" ");
            if(hours == 1) {
                sb.append(context.getResources().getString(R.string.snooze_hour));
            } else {
                sb.append(context.getResources().getString(R.string.snooze_hours));
            }
            if(min > 0 || seconds > 0)
                sb.append(", ");
        }
        if(min > 0){
            sb.append(min);
            sb.append(" ");
            if(min == 1) {
                sb.append(context.getResources().getString(R.string.snooze_min));
            } else {
                sb.append(context.getResources().getString(R.string.snooze_mins));
            }
            if(seconds > 0)
                sb.append(", ");
        }
        if(seconds > 0){
            sb.append(seconds);
            sb.append(" ");
            if(seconds == 1) {
                sb.append(context.getResources().getString(R.string.snooze_second));
            } else {
                sb.append(context.getResources().getString(R.string.snooze_seconds));
            }
        }

        return sb.toString();
    }
}
