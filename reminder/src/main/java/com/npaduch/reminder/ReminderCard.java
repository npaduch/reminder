package com.npaduch.reminder;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

/**
 * Created by nolanpaduch on 7/7/14.
 *
 * Custom cardview to show the attributes of the reminder
 */
class ReminderCard extends Card {

    private final static String TAG = "ReminderCard";

    // Tasks for ASYNC task
    private static final int ASYNC_TASK_UPDATE_REMINDER = 0;
    private static final int ASYNC_TASK_DELETE_REMINDER = 1;

    // reminder details to be displayed
    private Reminder reminder;

    // application context
    private final Context context;

    // fragment type the card will be shown in
    private final int fragmentType;

    /**
     * Constructor with a custom inner layout
     *
     * @param context - activity context
     */
    public ReminderCard(Context context, Reminder reminder, int fragmentType) {
        super(context, R.layout.reminder_card_entry);
        this.context = context;
        this.reminder = reminder;
        this.fragmentType = fragmentType;
        init();
    }

    /**
     * Init
     */
    private void init(){

        //Set a OnClickListener listener
        setOnClickListener(new MyCardClickListener());

        /** Card Header **/
        //Create a CardHeader
        ReminderCardHeader header = new ReminderCardHeader(context);

        //Add a popup menu. This method set OverFlow button to visible
        header.setPopupMenu(R.menu.card_menu, new MyOnClickCardHeaderPopupMenuListener());
        addCardHeader(header);

        /** Swipe to undo **/
        if(fragmentType == CardListFragment.LIST_PENDING) {
            // set ID
            setId(Integer.toString(reminder.getReminderID()));
            setSwipeable(true);
            setOnSwipeListener(new MyCardOnSwipeListener());


            setOnUndoSwipeListListener(new MyCardUndoSwipeListener());
        }


    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        TextView dateTimeTextView = (TextView) parent.findViewById(R.id.reminderCardDateTimeText);

        dateTimeTextView.setText(reminder.getDateTimeString(context));


    }

    public class ReminderCardHeader extends CardHeader {

        TextView descriptionTextView;

        public ReminderCardHeader(Context context) {
            super(context, R.layout.reminder_card_header);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            descriptionTextView = (TextView) parent.findViewById(R.id.reminderCardEntryHeaderText);

            descriptionTextView.setText(reminder.getDescription());
        }
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }



    private class MyCardOnSwipeListener implements Card.OnSwipeListener {

        @Override
        public void onSwipe(Card card) {
            Log.d(TAG, "Card swiped");
            ReminderCard rc = (ReminderCard) card;
            Reminder r = rc.getReminder();
            // set item completed
            r.setCompleted(true);
            // cancel alarm
            r.cancelAlarm(context);
            // clear reminder
            r.cancelNotification(context);
            // make change in file
            UpdateFile uf = new UpdateFile(
                    ASYNC_TASK_UPDATE_REMINDER,     // save updated reminder
                    r                               // reminder to be saved
            );
            uf.execute();
            // send to both since only the active one will receive it
            BusEvent busEvent = new BusEvent(BusEvent.TYPE_REMOVE, BusEvent.TARGET_PENDING, r);
            busEvent.addTarget(BusEvent.TARGET_COMPLETED);
            BusProvider.getInstance().post(busEvent);
        }
    }

    private class MyCardUndoSwipeListener implements OnUndoSwipeListListener {
        @Override
        public void onUndoSwipe(Card card) {
            Log.d(TAG, "Card swipe undone");
            ReminderCard rc = (ReminderCard) card;
            Reminder r = rc.getReminder();

            // set item not completed
            r.setCompleted(false);
            // reschedule alarm
            r.setAlarm(context);

            // make change in file
            UpdateFile uf = new UpdateFile(
                    ASYNC_TASK_UPDATE_REMINDER,     // save updated reminder
                    r                               // reminder to be saved
            );
            uf.execute();

            // send to both since only the active one will receive it
            BusEvent busEvent = new BusEvent(BusEvent.TYPE_ADD, BusEvent.TARGET_PENDING, r);
            busEvent.addTarget(BusEvent.TARGET_COMPLETED);
            BusProvider.getInstance().post(busEvent);
        }
    }

    private class MyCardClickListener implements Card.OnCardClickListener {
        @Override
        public void onClick(Card card, View view) {
            ReminderCard rc = (ReminderCard) card;
            Reminder r = rc.getReminder();
            Log.d(TAG, "Reminder clicked, will edit:");
            r.outputReminderToLog();
            editReminder(r);
        }
    };

    private class MyOnClickCardHeaderPopupMenuListener implements CardHeader.OnClickCardHeaderPopupMenuListener {
        @Override
        public void onMenuItemClick(BaseCard baseCard, MenuItem menuItem) {
            Log.d(TAG, "Card menu item clicked: "+menuItem.toString());
            ReminderCard rc = (ReminderCard) baseCard;
            Reminder r = rc.getReminder();
            r.outputReminderToLog();
            switch(menuItem.getItemId()){
                case R.id.action_share_reminder:
                    sendShareIntent(r);
                    break;
                case R.id.action_edit_reminder:
                    editReminder(r);
                    break;
                case R.id.action_delete_reminder:
                    deleteReminder(r);
                    break;
            }
        }
    };

    private void sendShareIntent(Reminder r){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, buildReminderString(r));
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    private void editReminder(Reminder r){
        // send to completed or pending, since only one is active right now
        BusEvent busEvent = new BusEvent(BusEvent.TYPE_EDIT_REMINDER, BusEvent.TARGET_PENDING, r);
        busEvent.addTarget(BusEvent.TARGET_COMPLETED);
        BusProvider.getInstance().post(busEvent);
    }

    private void deleteReminder(Reminder r){
        // send to completed or pending, since only one is active right now
        BusEvent busEvent = new BusEvent(BusEvent.TYPE_REMOVE, BusEvent.TARGET_PENDING, r);
        busEvent.addTarget(BusEvent.TARGET_COMPLETED);
        BusProvider.getInstance().post(busEvent);
        UpdateFile updateFile = new UpdateFile(ASYNC_TASK_DELETE_REMINDER, r);
        updateFile.execute();
    }

    private String buildReminderString(Reminder r){
        StringBuilder sb = new StringBuilder();
        sb.append(context.getResources().getString(R.string.share_reminder_title));
        sb.append("\n");
        sb.append(context.getResources().getString(R.string.share_reminder_description));
        sb.append(" ");
        sb.append(r.getDescription());
        sb.append("\n");
        sb.append(context.getResources().getString(R.string.share_reminder_time));
        sb.append(" ");
        sb.append(r.getSpecificDateTimeString(context));
        return sb.toString();
    }


    /** Asynchronous task for reading/writing to file **/
    private class UpdateFile extends AsyncTask {

        // Task to complete in the background
        final int task;
        // Reminder to manipulate (if we need to)
        final Reminder reminder;

        public UpdateFile(int task, Reminder r) {
            super();
            this.task = task;
            this.reminder = r;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            switch ( task ) {
                case ASYNC_TASK_UPDATE_REMINDER:
                    reminder.writeToFile(context);
                    break;
                case ASYNC_TASK_DELETE_REMINDER:
                    reminder.removeFromFile(context);
                    break;
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