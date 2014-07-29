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
import android.widget.TextView;

import com.squareup.otto.Subscribe;

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
// TODO: change transaction to hide to preserve fragment state

public class CardListFragment extends Fragment {

    private final static String TAG = "CardListFragment";

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated");

        // get fragment type
        fragmentType = getArguments().getInt(LIST_TYPE, LIST_PENDING);

        // save off context
        context = getActivity();

        ArrayList<Card> cardList = new ArrayList<Card>();

        // Set the adapter
        mCardArrayAdapter = new CardArrayAdapter(context, cardList);
        // Make swipes undo-able!
        mCardArrayAdapter.setEnableUndo(true);

        // Set ListView
        mCardListView = (CardListView) getView().findViewById(R.id.reminderCardListView);

        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }

        // load in reminders in the background
        startLoad();

        /** Initialize floating action button **/
        Fab mFab = (Fab)getView().findViewById(R.id.FloatingAddButton);
        mFab.setFabColor(getActivity().getResources().getColor(R.color.app_color_theme));
        mFab.setFabDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_new));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BusEvent event = new BusEvent(BusEvent.TYPE_CHANGE_FRAG, BusEvent.TARGET_MAIN);
                event.setToFragment(BusEvent.FRAGMENT_NEW_REMINDER);
                event.setFromFragment(BusEvent.FRAGMENT_PENDING);   // this doesn't matter
                BusProvider.getInstance().post(event);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cardlist, container, false);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "OnResume");
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void startLoad(){
        getActivity().setProgressBarIndeterminateVisibility(true);
        new LoadReminders(getActivity(), fragmentType).execute();
    }

    private void endLoad(ArrayList<Card> cards){

        // TODO: modify to sort
        mCardArrayAdapter.clear();
        for(Card c : cards){
            mCardArrayAdapter.add(c);
        }

        // update listview
        mCardArrayAdapter.notifyDataSetChanged();

        // set empty view if no reminders
        TextView tv = (TextView) getView().findViewById(R.id.cardListEmptyView);
        mCardListView.setEmptyView(tv);

        // stop progress circle
        getActivity().setProgressBarIndeterminateVisibility(false);
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

    public void removeReminderCard(Reminder r){

        // search for reminder and remove it
        for(int i = 0; i < mCardArrayAdapter.getCount(); i++){
            // find the view with Id that matches reminder
            ReminderCard rc = (ReminderCard) mCardArrayAdapter.getItem(i);
            Reminder cardReminder = rc.getReminder();
            if(cardReminder.getReminderID() == r.getReminderID()){
                // found a match, remove it
                mCardArrayAdapter.remove(rc);
                break;
            }
        }

        // update view
        mCardArrayAdapter.notifyDataSetChanged();
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

    // to do, replace this with bus
    public static void switchToEditFragment(Reminder r){
        BusEvent event = new BusEvent(BusEvent.TYPE_EDIT_REMINDER, BusEvent.TARGET_MAIN);
        event.setReminder(r);
        BusProvider.getInstance().post(event);
    }

    // todo: check fragment type
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

    /** Event bus listener **/
    @Subscribe
    public void BusEvent(BusEvent event){
        // check if it's for us
        if(event.getTargets().contains(BusEvent.TARGET_PENDING)) {
            Log.d(TAG, "Message received: " + event.getType());
            switch (event.getType()) {
                case BusEvent.TYPE_ADD:
                    addReminderCard(event.getReminder());
                    break;
                case BusEvent.TYPE_REMOVE:
                    removeReminderCard(event.getReminder());
                    break;
                case BusEvent.TYPE_LOAD_REMINDERS:
                    endLoad(event.getCardList());
            }
        } else if (event.getTargets().contains(BusEvent.TARGET_COMPLETED)) {
            Log.d(TAG, "Message received: " + event.getType());
            switch (event.getType()) {
                case BusEvent.TYPE_ADD:
                    addReminderCard(event.getReminder());
                    break;
                case BusEvent.TYPE_REMOVE:
                    removeReminderCard(event.getReminder());
                    break;
                case BusEvent.TYPE_LOAD_REMINDERS:
                    endLoad(event.getCardList());
            }
        }
    }

}


