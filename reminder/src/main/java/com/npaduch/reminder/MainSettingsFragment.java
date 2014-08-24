package com.npaduch.reminder;

import android.app.AlertDialog;
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

import com.doomonafireball.betterpickers.hmspicker.HmsPickerBuilder;
import com.doomonafireball.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by nolanpaduch on 8/14/14.
 *
 * Fragment to display the main settings for the app
 */

public class MainSettingsFragment extends Fragment
        implements  RadialTimePickerDialog.OnTimeSetListener,
                    HmsPickerDialogFragment.HmsPickerDialogHandler {

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

    private boolean BEFORE = false;
    private boolean AFTER = true;

    // for compare
    private final static int TIME_BEFORE = -1;
    private final static int TIME_EQUAL = 0;
    private final static int TIME_AFTER = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "New Reminder OnCreateView");

        // inflate view first
        rootView = inflater.inflate(R.layout.preference_layout, container, false);

        // save off context
        this.context = getActivity();

        // Create Cards
        Card timeCard = new Card(getActivity(), R.layout.card_time_preference);
        Card snoozeCard = new Card(getActivity(), R.layout.card_snooze_preference);

        // Set card contents
        CardView timeCardView = (CardView) rootView.findViewById(R.id.preferenceTimeLayout);
        timeCardView.setCard(timeCard);
        CardView snoozeCardView = (CardView) rootView.findViewById(R.id.preferenceSnoozeLayout);
        snoozeCardView.setCard(snoozeCard);

        initTimes();

        initSnooze();

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

    private void initSnooze(){
        SettingsHandler settingsHandler = new SettingsHandler();
        TextView snoozeValue = (TextView) rootView.findViewById(R.id.preferenceSnoozeCustomValue);
        snoozeValue.setText(settingsHandler.getSnoozeString(context));
    }

    public void assignOnClickListener(View rootView){
        LinearLayout morningView = (LinearLayout)rootView.findViewById(R.id.timePreferenceMorning);
        LinearLayout noonView = (LinearLayout)rootView.findViewById(R.id.timePreferenceNoon);
        LinearLayout afternoonView = (LinearLayout)rootView.findViewById(R.id.timePreferenceAfternoon);
        LinearLayout eveningView = (LinearLayout)rootView.findViewById(R.id.timePreferenceEvening);
        LinearLayout nightView = (LinearLayout)rootView.findViewById(R.id.timePreferenceNight);
        LinearLayout snoozeView = (LinearLayout)rootView.findViewById(R.id.preferenceSnoozeCustom);

        TimeOnClickListener timeOnClickListener = new TimeOnClickListener();
        morningView.setOnClickListener(timeOnClickListener);
        noonView.setOnClickListener(timeOnClickListener);
        afternoonView.setOnClickListener(timeOnClickListener);
        eveningView.setOnClickListener(timeOnClickListener);
        nightView.setOnClickListener(timeOnClickListener);

        SnoozeOnClickListener snoozeOnClickListener = new SnoozeOnClickListener();
        snoozeView.setOnClickListener(snoozeOnClickListener);
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

    public class SnoozeOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {Calendar now = Calendar.getInstance();
            HmsPickerBuilder hpb = new HmsPickerBuilder()
                    .setFragmentManager(getChildFragmentManager())
                    .setStyleResId(R.style.BetterPickersDialogFragment)
                    .setTargetFragment(MainSettingsFragment.this);
            hpb.show();
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        SettingsHandler settingsHandler = new SettingsHandler();
        Calendar nextTime = Calendar.getInstance();
        int compareResult = TIME_EQUAL;
        switch(timeViewId){
            case R.id.timePreferenceMorning:
                // check that next time is after new selection
                nextTime.setTimeInMillis(settingsHandler.getTimeNoon(getActivity()));
                compareResult = compareTimes(cal, nextTime);
                if(compareResult == TIME_AFTER || compareResult == TIME_EQUAL){
                    throwBadTimeDialog(AFTER, getActivity().getResources().getString(R.string.time_noon));
                    return;
                }
                settingsHandler.setTimeMorning(getActivity(), cal.getTimeInMillis());
                break;
            case R.id.timePreferenceNoon:
                // check that previous time is before new selection
                nextTime.setTimeInMillis(settingsHandler.getTimeMorning(getActivity()));
                compareResult = compareTimes(cal, nextTime);
                if(compareResult == TIME_BEFORE || compareResult == TIME_EQUAL){
                    throwBadTimeDialog(BEFORE, getActivity().getResources().getString(R.string.time_morning));
                    return;
                }
                // check that next time is after new selection
                nextTime.setTimeInMillis(settingsHandler.getTimeAfternoon(getActivity()));
                compareResult = compareTimes(cal, nextTime);
                if(compareResult == TIME_AFTER || compareResult == TIME_EQUAL){
                    throwBadTimeDialog(AFTER, getActivity().getResources().getString(R.string.time_afternoon));
                    return;
                }
                settingsHandler.setTimeNoon(getActivity(), cal.getTimeInMillis());
                break;
            case R.id.timePreferenceAfternoon:
                // check that previous time is before new selection
                nextTime.setTimeInMillis(settingsHandler.getTimeNoon(getActivity()));
                compareResult = compareTimes(cal, nextTime);
                if(compareResult == TIME_BEFORE || compareResult == TIME_EQUAL){
                    throwBadTimeDialog(BEFORE, getActivity().getResources().getString(R.string.time_noon));
                    return;
                }
                // check that next time is after new selection
                nextTime.setTimeInMillis(settingsHandler.getTimeEvening(getActivity()));
                compareResult = compareTimes(cal, nextTime);
                if(compareResult == TIME_AFTER || compareResult == TIME_EQUAL){
                    throwBadTimeDialog(AFTER, getActivity().getResources().getString(R.string.time_evening));
                    return;
                }
                settingsHandler.setTimeAfternoon(getActivity(), cal.getTimeInMillis());
                break;
            case R.id.timePreferenceEvening:
                // check that previous time is before new selection
                nextTime.setTimeInMillis(settingsHandler.getTimeAfternoon(getActivity()));
                compareResult = compareTimes(cal, nextTime);
                if(compareResult == TIME_BEFORE || compareResult == TIME_EQUAL){
                    throwBadTimeDialog(BEFORE, getActivity().getResources().getString(R.string.time_afternoon));
                    return;
                }
                // check that next time is after new selection
                nextTime.setTimeInMillis(settingsHandler.getTimeNight(getActivity()));
                compareResult = compareTimes(cal, nextTime);
                if(compareResult == TIME_AFTER || compareResult == TIME_EQUAL){
                    throwBadTimeDialog(AFTER, getActivity().getResources().getString(R.string.time_night));
                    return;
                }
                settingsHandler.setTimeEvening(getActivity(), cal.getTimeInMillis());
                break;
            case R.id.timePreferenceNight:
                // check that previous time is before new selection
                nextTime.setTimeInMillis(settingsHandler.getTimeEvening(getActivity()));
                compareResult = compareTimes(cal, nextTime);
                if(compareResult == TIME_BEFORE || compareResult == TIME_EQUAL){
                    throwBadTimeDialog(BEFORE, getActivity().getResources().getString(R.string.time_evening));
                    return;
                }
                settingsHandler.setTimeNight(getActivity(), cal.getTimeInMillis());
                break;
        }

        initTimes();
    }

    public void throwBadTimeDialog(boolean time, String nextTime){
        Log.d(TAG, "Selected time is later than next time bracket");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        StringBuilder sb = new StringBuilder();
        if(time == BEFORE)
            sb.append(context.getResources().getString(R.string.pref_times_bad_time_body1_before));
        else
            sb.append(context.getResources().getString(R.string.pref_times_bad_time_body1_after));
        sb.append(" ");
        sb.append(nextTime.toLowerCase());
        sb.append(".\n\n");
        sb.append(context.getResources().getString(R.string.pref_times_bad_time_body2));

        builder.setMessage(sb.toString())
                .setTitle(R.string.pref_times_bad_time_title)
                .setPositiveButton(R.string.pref_times_bad_time_button, null)
                .create()
                .show();
    }

    /**
     * Compare two calendars in respect to hour and minutes
     * @param cal1
     * @param cal2
     * @return
     *      -1 if cal 1 before cal 2
     *      0 if equal
     *      1 if cal 1 after cal 2
     */
    public int compareTimes(Calendar cal1, Calendar cal2){
        long hour1 = cal1.get(Calendar.HOUR_OF_DAY);
        long min1 = cal1.get(Calendar.MINUTE);
        long hour2 = cal2.get(Calendar.HOUR_OF_DAY);
        long min2 = cal2.get(Calendar.MINUTE);

        if(hour1 < hour2){
            return TIME_BEFORE;
        } else if(hour1 == hour2){
            if(min1 < min2)
                return TIME_BEFORE;
            else if(min1 == min2)
                return TIME_EQUAL;
        }

        return TIME_AFTER;
    }



    @Override
    public void onDialogHmsSet(int reference, int hours, int minutes, int seconds) {
        SettingsHandler settingsHandler = new SettingsHandler();
        long msTime = hours * 60 * 60 * 1000;
        msTime += minutes * 60 * 1000;
        msTime += seconds * 1000;

        if(msTime <= 0)
            throwBadSnoozeDialog();
        else{
            settingsHandler.setCustomReminderSnooze(context, msTime);
            initSnooze();
        }
    }

    public void throwBadSnoozeDialog(){
        Log.d(TAG, "Selected snooze time is <= 0");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.pref_snooze_error_message)
                .setTitle(R.string.pref_times_bad_time_title)
                .setPositiveButton(R.string.pref_times_bad_time_button, null)
                .create()
                .show();
    }

}

