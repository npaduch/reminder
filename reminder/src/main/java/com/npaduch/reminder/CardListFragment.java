package com.npaduch.reminder;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by nolanpaduch on 5/3/14.
 *
 */

// TODO: Add samples if first time launching
// TODO: BUG: hitting back on listview clears view

public class CardListFragment extends Fragment {

    private final static String TAG = "CardListFragment";

    static FragmentCommunicationListener messenger;

    // Card adapter and listview
    public CardArrayAdapter mCardArrayAdapter;
    public CardListView mCardListView;

    // Arguments
    public static final String LIST_TYPE = "list_type";
    public static final int LIST_PENDING = 0;
    public static final int LIST_COMPLETED = 1;

    // Tasks for ASYNC task
    private static final int ASYNC_TASK_READ_REMINDERS = 0;
    private static final int ASYNC_TASK_WRITE_REMINDERS = 1;
    private static final int ASYNC_TASK_UPDATE_REMINDER = 2;
    private static final int ASYNC_TASK_DELETE_REMINDER = 3;

    // list type
    public int fragmentType = LIST_PENDING;

    public static Context context;

    public CardListFragment() {
    }

    // Container Activity must implement this interface
    public interface FragmentCommunicationListener {
        public void send(Bundle message);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            messenger = (FragmentCommunicationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CardListFragment.FragmentCommunicationListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // get fragment type
        fragmentType = getArguments().getInt(LIST_TYPE, LIST_PENDING);

        // save off context
        context = getActivity();

        // TODO: Move this to the background
        populateReminders();
        ArrayList<Card> cardList = getCards();

        Log.d(TAG, "Found "+cardList.size()+" cards.");

        // Set the adapter
        mCardArrayAdapter = new CardArrayAdapter(getActivity(), cardList);
        // Make swipes undo-able!
        mCardArrayAdapter.setEnableUndo(true);

        mCardListView = (CardListView) getActivity().findViewById(R.id.reminderCardListView);
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cardlist, container, false);
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_add_reminder:
                Bundle b = new Bundle();
                b.putInt(MainActivity.MESSAGE_TASK, MainActivity.TASK_CHANGE_FRAG);
                b.putInt(MainActivity.TASK_INT, MainActivity.NEW_REMINDER);
                messenger.send(b);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void populateReminders(){

        // Check for reminders in file
        MainActivity.reminders = getJSONFileContents();

        if(MainActivity.reminders == null)
            MainActivity.reminders = new ArrayList<Reminder>();

        // Get all the pending reminders
        MainActivity.syncReminders();

    }

    private ArrayList<Card> getCards(){
        // array list of reminders
        ArrayList<Reminder> reminders;
        // array list of reminder Cards
        ArrayList<Card> cards = new ArrayList<Card>();

        // get reminders
        if(fragmentType == LIST_PENDING)
            reminders = MainActivity.pendingReminders;
        else
            reminders = MainActivity.completedReminders;

        // create cards
        for ( Reminder r : reminders ){
            cards.add(new ReminderCard(getActivity(), r));
        }

        return cards;
    }

    public void addReminderCard(Reminder r){

        // Check if reminder already exists
        boolean found = false;
        for(int i = 0; i < mCardArrayAdapter.getCount(); i++){
            // find the view with Id that matches reminder
            ReminderCard rc = (ReminderCard) mCardArrayAdapter.getItem(i);
            Reminder cardReminder = rc.getReminder();
            if(cardReminder.getReminderID() == r.getReminderID()){
                // found a match, update it with new card
                found = true;
                rc.setReminder(r);
                break;
            }
        }

        // if we didn't find it, it must be new! Add it.
        // TODO: sort, adding to beginning for now
        if(!found)
            mCardArrayAdapter.insert(new ReminderCard(getActivity(), r), 0);

        // update view
        mCardArrayAdapter.notifyDataSetChanged();
    }


    /** This is different from the reminder class's instance!
     *  It will create a new file if one is not found.
     */
    private ArrayList<Reminder> getJSONFileContents(){
        Log.d(TAG, "Looking for file " + getActivity().getFilesDir() + File.pathSeparator + Reminder.filename);

        // Check for file
        File file = new File(getActivity().getFilesDir(), Reminder.filename);
        if(!file.exists()){
            // it doesn't exist. Write an empty JSON array to it
            Log.d(TAG, "Initializing JSON file.");
            try {
                Reminder.initFile(getActivity());
            } catch (Exception e){
                Log.e(TAG, "Error creating JSON file: "+e);
            }
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

    public static Card.OnCardClickListener cardClickListener = new Card.OnCardClickListener(){
        @Override
        public void onClick(Card card, View view) {
            ReminderCard rc = (ReminderCard) card;
            Reminder r = rc.getReminder();
            Log.d(TAG, "Reminder clicked, will edit:");
            r.outputReminderToLog();
            switchToEditFragment(r);
        }
    };

    public static CardHeader.OnClickCardHeaderPopupMenuListener cardOverflowClickListener
            = new CardHeader.OnClickCardHeaderPopupMenuListener(){
        @Override
        public void onMenuItemClick(BaseCard baseCard, MenuItem menuItem) {
            Log.d(TAG, "Card menu item clicked: "+menuItem.toString());
            ReminderCard rc = (ReminderCard) baseCard;
            Reminder r = rc.getReminder();
            r.outputReminderToLog();
            switch(menuItem.getItemId()){
                case R.id.action_share_reminder:
                    break;
                case R.id.action_edit_reminder:
                    switchToEditFragment(r);
                    break;
                case R.id.action_delete_reminder:
                    break;
            }
        }
    };

    public static void switchToEditFragment(Reminder r){
        int mainReminderOffset = MainActivity.reminders.indexOf(r);
        Bundle b = new Bundle();
        b.putInt(MainActivity.MESSAGE_TASK, MainActivity.TASK_EDIT_REMINDER);
        b.putInt(MainActivity.TASK_INT, mainReminderOffset);
        b.putInt(MainActivity.TASK_INITIATOR, MainActivity.PENDING_REMINDERS);
        messenger.send(b);
    }

    public static Card.OnSwipeListener cardOnSwipeListener
            = new Card.OnSwipeListener() {
        @Override
        public void onSwipe(Card card) {
            Log.d(TAG, "Card swiped");
            ReminderCard rc = (ReminderCard) card;
            Reminder r = rc.getReminder();
            dismissItem(r);

        }
    };

    public static Card.OnUndoSwipeListListener cardOnUndoSwipeListener
            = new Card.OnUndoSwipeListListener(){
        @Override
        public void onUndoSwipe(Card card) {
            Log.d(TAG, "Card swipe undone");
            ReminderCard rc = (ReminderCard) card;
            Reminder r = rc.getReminder();
            undismissItem(r);
        }
    };

    private static void dismissItem(Reminder r){
        Log.d(TAG, "Dismissing item:"+r.getDescription());
        // set item completed
        r.setCompleted(true);
        // cancel alarm
        r.cancelAlarm(context);
        // make change in file
        UpdateFile uf = new UpdateFile(
                ASYNC_TASK_UPDATE_REMINDER,     // save updated reminder
                r,                              // reminder to be saved
                true                            // read file back into lists when done
        );
        uf.execute();
    }

    private static void undismissItem(Reminder r){
        Log.d(TAG, "Undismissing item:"+r.getDescription());
        // set item not completed
        r.setCompleted(false);
        // reschedule alarm
        r.setAlarm(context);
        // make change in file
        UpdateFile uf = new UpdateFile(
                ASYNC_TASK_UPDATE_REMINDER,     // save updated reminder
                r,                              // reminder to be saved
                true                            // read file back into lists when done
        );
        uf.execute();
    }

    private void permanentlyDeleteItem(Reminder r){
        Log.d(TAG, "Permanently deleting item:"+r.getDescription());
        // remove from file
        UpdateFile uf = new UpdateFile(
                ASYNC_TASK_DELETE_REMINDER,     // delete reminder
                r,                              // reminder to be saved
                true                            // read file back into lists when done
        );
        uf.execute();
    }

    private void recreateDeletedItem(Reminder r){
        Log.d(TAG, "Undeleting item:"+r.getDescription());
        // remove from file
        UpdateFile uf = new UpdateFile(
                ASYNC_TASK_UPDATE_REMINDER,     // delete reminder
                r,                              // reminder to be saved
                true                            // read file back into lists when done
        );
        uf.execute();
    }

    /** Asynchronous task for reading/writing to file **/
    private static class UpdateFile extends AsyncTask {

        // Task to complete in the background
        int task;
        // Reminder to manipulate (if we need to)
        Reminder reminder;
        // sync files with current status
        boolean sync;

        public UpdateFile(int task, Reminder r, boolean sync) {
            super();
            this.task = task;
            this.reminder = r;
            this.sync = sync;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            switch ( task ) {
                case ASYNC_TASK_READ_REMINDERS:
                    break;
                case ASYNC_TASK_WRITE_REMINDERS:
                    break;
                case ASYNC_TASK_UPDATE_REMINDER:
                    reminder.writeToFile(context);
                    break;
                case ASYNC_TASK_DELETE_REMINDER:
                    reminder.removeFromFile(context);
                    break;
            }
            if(sync){
                MainActivity.reminders = Reminder.getJSONFileContents(context);
                MainActivity.syncReminders();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "AsyncTask onPreExecute");
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.d(TAG, "AsyncTask onPostExecute");
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }
    }
}
