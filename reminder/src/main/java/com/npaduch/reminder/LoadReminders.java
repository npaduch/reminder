package com.npaduch.reminder;

/**
 * Created by nolanpaduch on 7/22/14.
 *
 * Asynchronous task to load reminders into view.
 * Send events as reminders are loaded.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;

/** Asynchronous task for loading reminders initially **/
class LoadReminders extends AsyncTask {

    private final String TAG = "LoadReminders";

    private final Context context;

    private final int fragmentType;

    private ArrayList<Card> cards;

    public LoadReminders(Context context, int fragmentType) {
        this.context = context;
        this.fragmentType = fragmentType;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        populateReminders();
        cards = new ArrayList<Card>();
        cards = getCards();
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "LoadReminders onPreExecute");
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Log.d(TAG, "LoadReminders onPostExecute");

        BusEvent event;
        if(fragmentType == CardListFragment.LIST_PENDING) {
            event = new BusEvent(BusEvent.TYPE_LOAD_REMINDERS, BusEvent.TARGET_PENDING);
        } else {
            event = new BusEvent(BusEvent.TYPE_LOAD_REMINDERS, BusEvent.TARGET_COMPLETED);
        }
        event.setCardList(cards);
        BusProvider.getInstance().post(event);


    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
    }



    private void populateReminders(){

        // Check for reminders in file
        MainActivity.reminders = initReminderList();

        if(MainActivity.reminders == null)
            MainActivity.reminders = new ArrayList<Reminder>();

        // Get all the pending reminders
        MainActivity.syncReminders();

    }


    /**
     * This checks and creates the file if neccessary.
     * Then it gets the reminders from the JSON file.
     */
    private ArrayList<Reminder> initReminderList(){
        Log.d(TAG, "Looking for file " + context.getFilesDir() + File.pathSeparator + Reminder.filename);

        // Check for file
        File file = new File(context.getFilesDir(), Reminder.filename);
        if(!file.exists()){
            // it doesn't exist. Write an empty JSON array to it
            Log.d(TAG, "Initializing JSON file.");
            try {
                Reminder.initFile(context);
            } catch (Exception e){
                Log.e(TAG, "Error creating JSON file: "+e);
            }
            return null;
        }
        Log.d(TAG, "JSON file found. Loading in reminders.");

        return Reminder.getJSONFileContents(context);
    }


    private ArrayList<Card> getCards(){
        // array list of reminders
        ArrayList<Reminder> reminders;
        // array list of reminder Cards
        ArrayList<Card> cards = new ArrayList<Card>();

        // get reminders
        if(fragmentType == CardListFragment.LIST_PENDING)
            reminders = MainActivity.pendingReminders;
        else
            reminders = MainActivity.completedReminders;

        // create cards
        for ( Reminder r : reminders ){
            cards.add(new ReminderCard(context, r, fragmentType));
        }

        return cards;
    }
}