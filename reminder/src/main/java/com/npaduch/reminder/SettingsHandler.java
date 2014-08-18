package com.npaduch.reminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by nolanpaduch on 8/12/14.
 *
 * This class will handle getting and saving preferences
 */
public class SettingsHandler {

    private final static String TAG = "SettingsHandler";

    private final static String nextId = "Next_ID";
    private final static String timeMorning = "time_morning";
    private final static String timeNoon = "time_noon";
    private final static String timeAfternoon = "time_afternoon";
    private final static String timeEvening = "time_evening";
    private final static String timeNight = "time_night";

    private final static long timeDefaultMorning = 1000 * 60 * 60 * 9;
    private final static long timeDefaultNoon = 1000 * 60 * 60 * 12;
    private final static long timeDefaultAfternoon = 1000 * 60 * 60 * (12 + 3);
    private final static long timeDefaultEvening = 1000 * 60 * 60 * (12 + 5);
    private final static long timeDefaultNight = 1000 * 60 * 60 * (12 + 8);

    private final static int badId = -1;
    private final static int badTime = -1;

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
            editor.putLong(timeMorning, timeDefaultMorning); // value to store
            editor.commit();
        }
        return sp.getLong(timeMorning, badTime);
    }

    public long getTimeNoon(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(timeNoon)){
            editor.putLong(timeNoon, timeDefaultNoon); // value to store
            editor.commit();
        }
        return sp.getLong(timeNoon, badTime);
    }

    public long getTimeAfternoon(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(timeAfternoon)){
            editor.putLong(timeAfternoon, timeDefaultAfternoon); // value to store
            editor.commit();
        }
        return sp.getLong(timeAfternoon, badTime);
    }

    public long getTimeEvening(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(timeEvening)){
            editor.putLong(timeEvening, timeDefaultEvening); // value to store
            editor.commit();
        }
        return sp.getLong(timeEvening, badTime);
    }

    public long getTimeNight(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        // handle case where ID has never been set
        if(!sp.contains(timeNight)){
            editor.putLong(timeNight, timeDefaultNight); // value to store
            editor.commit();
        }
        return sp.getLong(timeNight, badTime);
    }

}
