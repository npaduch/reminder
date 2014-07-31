package com.npaduch.reminder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

/**
 * Created by nolanpaduch on 7/7/14.
 *
 * Custom cardview to show the attributes of the reminder
 */
public class ReminderCard extends Card {

    private final static String TAG = "ReminderCard";

    // Tasks for ASYNC task
    private static final int ASYNC_TASK_UPDATE_REMINDER = 0;
    private static final int ASYNC_TASK_DELETE_REMINDER = 1;

    // views that will be presented
    private TextView dateTimeTextView;

    // reminder details to be displayed
    private Reminder reminder;

    // application context
    Context context;

    /**
     * Constructor with a custom inner layout
     * @param context
     */
    public ReminderCard(Context context, Reminder reminder) {
        this(context, R.layout.reminder_card_entry, reminder);
    }

    /**
     *
     * @param context
     * @param innerLayout
     */
    public ReminderCard(Context context, int innerLayout, Reminder reminder) {
        super(context, innerLayout);
        this.context = context;
        this.reminder = reminder;
        init();
    }

    /**
     * Init
     */
    private void init(){

        //Set a OnClickListener listener
        setOnClickListener(CardListFragment.cardClickListener);

        /** Card Header **/
        //Create a CardHeader
        ReminderCardHeader header = new ReminderCardHeader(context);

        //Add a popup menu. This method set OverFlow button to visible
        header.setPopupMenu(R.menu.card_menu, CardListFragment.cardOverflowClickListener);
        addCardHeader(header);

        /** Swipe to undo **/
        // set ID
        setId(Integer.toString(reminder.getReminderID()));
        setSwipeable(true);
        //setOnSwipeListener(CardListFragment.cardOnSwipeListener);
        setOnSwipeListener(new MyCardOnSwipeListener());
        //setOnUndoSwipeListListener(new MyCardUndoSwipeListener());

        setOnUndoSwipeListListener(new MyCardUndoSwipeListener());


    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        dateTimeTextView = (TextView) parent.findViewById(R.id.reminderCardDateTimeText);

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



    public class MyCardOnSwipeListener implements Card.OnSwipeListener {

        @Override
        public void onSwipe(Card card) {
            Log.d(TAG, "Card swiped");
            ReminderCard rc = (ReminderCard) card;
            Reminder r = rc.getReminder();
            // set item completed
            r.setCompleted(true);
            // cancel alarm
            r.cancelAlarm(context);
            // make change in file
            UpdateFile uf = new UpdateFile(
                    ASYNC_TASK_UPDATE_REMINDER,     // save updated reminder
                    r                               // reminder to be saved
            );
            uf.execute();
            BusProvider.getInstance().post(new BusEvent(BusEvent.TYPE_REMOVE, BusEvent.TARGET_PENDING, r));
        }
    }

    public class MyCardUndoSwipeListener implements OnUndoSwipeListListener {
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

            BusProvider.getInstance().post(new BusEvent(BusEvent.TYPE_ADD, BusEvent.TARGET_PENDING, r));
        }
    };


    /** Asynchronous task for reading/writing to file **/
    private class UpdateFile extends AsyncTask {

        // Task to complete in the background
        int task;
        // Reminder to manipulate (if we need to)
        Reminder reminder;

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