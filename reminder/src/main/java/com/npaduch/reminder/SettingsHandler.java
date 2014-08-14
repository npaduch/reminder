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
    private final static int badId = -1;

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

}
