package com.npaduch.reminder;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by nolanpaduch on 8/14/14.
 *
 * Fragment to display the main settings for the app
 */

public class MainSettingsFragment extends Fragment {

    private final static String TAG = "MainSettings";

    // View for fragment
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "New Reminder OnCreateView");

        // inflate view first
        rootView = inflater.inflate(R.layout.preference_layout, container, false);


        // Create a Card
        Card card = new Card(getActivity(), R.layout.card_time_preference);

        // Set card in the cardView
        CardView cardView = (CardView) rootView.findViewById(R.id.preferenceTimeLayout);
        cardView.setCard(card);

        initDefaultTimes(getActivity(), rootView);

        return rootView;
    }

    private void initDefaultTimes(Context context, View rootview){
        TextView morningValue = (TextView)rootView.findViewById(R.id.timePreferenceValueMorning);
        TextView noonValue = (TextView)rootView.findViewById(R.id.timePreferenceValueNoon);
        TextView afternoonValue = (TextView)rootView.findViewById(R.id.timePreferenceValueAfternoon);
        TextView eveningValue = (TextView)rootView.findViewById(R.id.timePreferenceValueEvening);
        TextView nightValue = (TextView)rootView.findViewById(R.id.timePreferenceValueNight);

        Calendar cal = Calendar.getInstance();
        SettingsHandler settingsHandler = new SettingsHandler();

        cal.setTimeInMillis(settingsHandler.getTimeMorning(context));
        morningValue.setText(Reminder.buildTimeString(context, cal));
        cal.setTimeInMillis(settingsHandler.getTimeNoon(context));
        noonValue.setText(Reminder.buildTimeString(context, cal));
        cal.setTimeInMillis(settingsHandler.getTimeAfternoon(context));
        afternoonValue.setText(Reminder.buildTimeString(context, cal));
        cal.setTimeInMillis(settingsHandler.getTimeEvening(context));
        eveningValue.setText(Reminder.buildTimeString(context, cal));
        cal.setTimeInMillis(settingsHandler.getTimeNight(context));
        nightValue.setText(Reminder.buildTimeString(context, cal));
    }


}

