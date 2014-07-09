package com.npaduch.reminder;

import android.content.Context;
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
        setOnSwipeListener(CardListFragment.cardOnSwipeListener);
        setOnUndoSwipeListListener(CardListFragment.cardOnUndoSwipeListener);


    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        Log.d(TAG, "Setting up inner view elements");

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
}