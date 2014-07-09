package com.npaduch.reminder;

import android.content.Context;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;

/**
 * Created by nolanpaduch on 7/7/14.
 *
 * Custom array adapter for card list view
 */
public class ReminderCardArrayAdapter  extends CardArrayAdapter {

    /**
     * Constructor
     *
     * @param context The current context.
     * @param cards   The cards to represent in the ListView.
     */
    public ReminderCardArrayAdapter(Context context, List<Card> cards) {
        super(context, cards);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}
