package com.npaduch.reminder;

import android.content.Context;
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

public class CardListFragment extends Fragment {

    private final static String TAG = "CardListFragment";

    // Card adapter and listview
    private CardArrayAdapter mCardArrayAdapter;
    private CardListView mCardListView;

    // Arguments
    public static final String LIST_TYPE = "list_type";
    public static final int LIST_PENDING = 0;
    public static final int LIST_COMPLETED = 1;

    // list type
    private int fragmentType = LIST_PENDING;

    private static Context context;

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

        // set title
        if(fragmentType == LIST_PENDING) {
            getActivity().getActionBar().setTitle(R.string.pending_title);
        } else {
            getActivity().getActionBar().setTitle(R.string.completed_title);
        }
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
            // no items.. placeholder
        }
        return super.onOptionsItemSelected(item);
    }

    private void startLoad(){
        getActivity().setProgressBarIndeterminateVisibility(true);
        //noinspection unchecked
        new LoadReminders(getActivity(), fragmentType).execute();
    }

    private void endLoad(ArrayList<Card> cards){

        mCardArrayAdapter.clear();
        for(Card c : cards){
            int i = 0;
            // for pending list, we want earliest at the top
            // for completed, we want latest at the top
            if(fragmentType == LIST_PENDING) {
                while (i < mCardArrayAdapter.getCount() &&
                        ((ReminderCard) c).getReminder().getMsTime() > ((ReminderCard) mCardArrayAdapter.getItem(i)).getReminder().getMsTime())
                    i++;
            } else {
                while (i < mCardArrayAdapter.getCount() &&
                        ((ReminderCard) c).getReminder().getMsTime() < ((ReminderCard) mCardArrayAdapter.getItem(i)).getReminder().getMsTime())
                    i++;
            }
            mCardArrayAdapter.insert(c, i);
        }

        // update listview
        mCardArrayAdapter.notifyDataSetChanged();

        // set empty view if no reminders
        TextView tv = (TextView) getView().findViewById(R.id.cardListEmptyView);
        mCardListView.setEmptyView(tv);

        // set undoable
        mCardArrayAdapter.setEnableUndo(true);

        // stop progress circle
        getActivity().setProgressBarIndeterminateVisibility(false);
    }


    void addReminderCard(Reminder r){

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
        if(!found) {
            boolean inserted = false;
            if(fragmentType == LIST_PENDING) {
                for (int i = 0; i < mCardArrayAdapter.getCount(); i++) {
                    if (r.getMsTime() < ((ReminderCard) mCardArrayAdapter.getItem(i)).getReminder().getMsTime()) {
                        mCardArrayAdapter.insert(new ReminderCard(getActivity(), r), i);
                        inserted = true;
                        break;
                    }
                }
            } else {
                for (int i = 0; i < mCardArrayAdapter.getCount(); i++) {
                    if (r.getMsTime() > ((ReminderCard) mCardArrayAdapter.getItem(i)).getReminder().getMsTime()) {
                        mCardArrayAdapter.insert(new ReminderCard(getActivity(), r), i);
                        inserted = true;
                        break;
                    }
                }
            }
            // check if it was inserted anywhere. If not, just add it to the end
            if(!inserted){
                mCardArrayAdapter.insert(new ReminderCard(getActivity(), r), mCardArrayAdapter.getCount());
            }
        }

        // update view
        mCardArrayAdapter.notifyDataSetChanged();
    }

    void removeReminderCard(Reminder r){

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



    public static final Card.OnCardClickListener cardClickListener = new Card.OnCardClickListener(){
        @Override
        public void onClick(Card card, View view) {
            ReminderCard rc = (ReminderCard) card;
            Reminder r = rc.getReminder();
            Log.d(TAG, "Reminder clicked, will edit:");
            r.outputReminderToLog();
            switchToEditFragment(r);
        }
    };

    public static final CardHeader.OnClickCardHeaderPopupMenuListener cardOverflowClickListener
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
    private static void switchToEditFragment(Reminder r){
        BusEvent event = new BusEvent(BusEvent.TYPE_EDIT_REMINDER, BusEvent.TARGET_MAIN);
        event.setReminder(r);
        BusProvider.getInstance().post(event);
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


