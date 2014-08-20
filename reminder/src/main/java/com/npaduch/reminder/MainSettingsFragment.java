package com.npaduch.reminder;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

import java.util.Calendar;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by nolanpaduch on 8/14/14.
 *
 * Fragment to display the main settings for the app
 */

public class MainSettingsFragment extends Fragment
        implements RadialTimePickerDialog.OnTimeSetListener {

    private final static String TAG = "MainSettings";

    // Keep track of time picker state
    private boolean mHasDialogFrame;
    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";

    // View for fragment
    private View rootView;

    // Save off context
    Context context;

    // hold ID for when time picker returns
    private int timeViewId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "New Reminder OnCreateView");

        // inflate view first
        rootView = inflater.inflate(R.layout.preference_layout, container, false);

        // save off context
        this.context = getActivity();

        // Create a Card
        Card card = new Card(getActivity(), R.layout.card_time_preference);

        // Set card in the cardView
        CardView cardView = (CardView) rootView.findViewById(R.id.preferenceTimeLayout);
        cardView.setCard(card);

        initTimes();

        assignOnClickListener(rootView);

        return rootView;
    }

    private void initTimes(){
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

    public void assignOnClickListener(View rootView){
        LinearLayout morningView = (LinearLayout)rootView.findViewById(R.id.timePreferenceMorning);
        LinearLayout noonView = (LinearLayout)rootView.findViewById(R.id.timePreferenceNoon);
        LinearLayout afternoonView = (LinearLayout)rootView.findViewById(R.id.timePreferenceAfternoon);
        LinearLayout eveningView = (LinearLayout)rootView.findViewById(R.id.timePreferenceEvening);
        LinearLayout nightView = (LinearLayout)rootView.findViewById(R.id.timePreferenceNight);

        TimeOnClickListener onClickListener = new TimeOnClickListener();
        morningView.setOnClickListener(onClickListener);
        noonView.setOnClickListener(onClickListener);
        afternoonView.setOnClickListener(onClickListener);
        eveningView.setOnClickListener(onClickListener);
        nightView.setOnClickListener(onClickListener);
    }

    public class TimeOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {Calendar now = Calendar.getInstance();
            SettingsHandler settingsHandler = new SettingsHandler();
            Calendar cal = Calendar.getInstance();

            switch(view.getId()){
                case R.id.timePreferenceMorning:
                    cal.setTimeInMillis(settingsHandler.getTimeMorning(context));
                    break;
                case R.id.timePreferenceNoon:
                    cal.setTimeInMillis(settingsHandler.getTimeNoon(context));
                    break;
                case R.id.timePreferenceAfternoon:
                    cal.setTimeInMillis(settingsHandler.getTimeAfternoon(context));
                    break;
                case R.id.timePreferenceEvening:
                    cal.setTimeInMillis(settingsHandler.getTimeEvening(context));
                    break;
                case R.id.timePreferenceNight:
                    cal.setTimeInMillis(settingsHandler.getTimeNight(context));
                    break;
            }

            timeViewId = view.getId();

            RadialTimePickerDialog timePickerDialog;

            timePickerDialog = RadialTimePickerDialog
                    .newInstance(MainSettingsFragment.this, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                            DateFormat.is24HourFormat(getActivity())
                    );

            if (mHasDialogFrame) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

                ft.add(R.id.content_frame, timePickerDialog, FRAG_TAG_TIME_PICKER);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            } else {
                timePickerDialog.show(getActivity().getSupportFragmentManager(), FRAG_TAG_TIME_PICKER);
            }
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        SettingsHandler settingsHandler = new SettingsHandler();
        switch(timeViewId){
            case R.id.timePreferenceMorning:
                settingsHandler.setTimeMorning(getActivity(), cal.getTimeInMillis());
                break;
            case R.id.timePreferenceNoon:
                settingsHandler.setTimeNoon(getActivity(), cal.getTimeInMillis());
                break;
            case R.id.timePreferenceAfternoon:
                settingsHandler.setTimeAfternoon(getActivity(), cal.getTimeInMillis());
                break;
            case R.id.timePreferenceEvening:
                settingsHandler.setTimeEvening(getActivity(), cal.getTimeInMillis());
                break;
            case R.id.timePreferenceNight:
                settingsHandler.setTimeNight(getActivity(), cal.getTimeInMillis());
                break;
        }

        initTimes();

    }

}

