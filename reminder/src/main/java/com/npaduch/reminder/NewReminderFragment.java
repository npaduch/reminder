package com.npaduch.reminder;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


/**
 * Created by nolanpaduch on 5/8/14.
 *
 * Fragment to create a new reminder.
 */

/** To-do list for this fragment */
// TODO: Make a way to edit reminders
// TODO: Make sure time is in the future
// TODO: Recurring reminders
// TODO: Verify date + time actually selected (i.e. back button clicked on specific date)

public class NewReminderFragment extends Fragment
        implements RadialTimePickerDialog.OnTimeSetListener,
        CalendarDatePickerDialog.OnDateSetListener {

    // Logging
    public final static String TAG = "NewReminderFragment";

    // delay time for keyboard popup on entry
    public final static int KEYBOARD_POPUP_DELAY = 200;

    // Time offsets
    public static final int TIME_MORNING    = 0;
    public static final int TIME_NOON       = 1;
    public static final int TIME_AFTERNOON  = 2;
    public static final int TIME_EVENING    = 3;
    public static final int TIME_NIGHT      = 4;
    public static final int TIME_OTHER      = 5;
    // Date offsets
    public static final int DATE_TODAY    = 0;
    public static final int DATE_TOMORROW = 1;
    public static final int DATE_OTHER    = 2;

    // Communication with main activity
    FragmentCommunicationListener messenger;
    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";
    private static final String FRAG_TAG_DATE_PICKER = "datePickerDialogFragment";

    // Keep track of time picker state
    private boolean mHasDialogFrame;

    // Preserve previous spinner date and time
    private boolean customTimeSelected = false;
    private int spinner_hour = 0;
    private int spinner_minute = 0;
    private boolean customDateSelected = false;
    private int spinner_year = 0;
    private int spinner_month = 0;
    private int spinner_day = 0;

    // Used to find bundled args
    public static final String REMINDER_OFFSET = "reminder_bundle";
    public static final int REMINDER_NOT_FOUND = -1;


    // Main view
    View rootView;
    // spinners
    ArrayAdapter<CharSequence> dayAdapter;
    ArrayAdapter<CharSequence> timeAdapter;
    // handle the case if set by code
    private boolean daySpinnerSetInCode = false;
    private boolean timeSpinnerSetInCode = false;

    private Reminder reminderHolder;

    public NewReminderFragment() {
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
                    + " must implement NewNoteFragment.FragmentCommunicationListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "New Reminder OnCreateView");
        rootView = inflater.inflate(R.layout.new_reminder, container, false);

        // initialize Spinners with string data
        initializeSpinners(rootView);

        // Set on click listeners
        TextView createButton = (TextView) rootView.findViewById(R.id.newReminderCreateButton);
        createButton.setOnClickListener(newReminderOnClickListener);

        // for time picker
        if (savedInstanceState == null) {
            mHasDialogFrame = rootView.findViewById(R.id.content_frame) != null;
        }

        // Set focus on edit text
        EditText et = (EditText) rootView.findViewById(R.id.newReminderEditText);

        // show keyboard on fragment entry
        et.postDelayed(new Runnable() {
            public void run() {
                EditText editText = (EditText) rootView.findViewById(R.id.newReminderEditText);

                editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                // If something already typed, go to end of section
                editText.setSelection(editText.getText().length());
            }
        }, KEYBOARD_POPUP_DELAY);

        // Enable menu items for this fragment
        setHasOptionsMenu(true);

        // initialize Reminder
        reminderHolder = new Reminder();

        // Check if we're actually editing a note
        if(getArguments() != null) {
            int reminderOffset = getArguments().getInt(REMINDER_OFFSET, REMINDER_NOT_FOUND);
            Log.d(TAG, "Reminder offset: " + reminderOffset);
            if (reminderOffset == REMINDER_NOT_FOUND) {
                Log.e(TAG,"Reminder offset passed does not exist. Can't edit.");
            } else {
                // we must be editing an old reminder
                initializeEdit(MainActivity.reminders.get(reminderOffset));
                reminderHolder = MainActivity.reminders.get(reminderOffset);
            }
        }


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.new_reminder_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_cancel_new_reminder:
                Bundle b = new Bundle();
                b.putInt(MainActivity.MESSAGE_TASK, MainActivity.TASK_CHANGE_FRAG);
                b.putInt(MainActivity.TASK_INT, MainActivity.REMINDER_LIST);
                messenger.send(b);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public  void initializeSpinners(View view){

        // locate spinners
        AlwaysChangeSpinner daySpinner = (AlwaysChangeSpinner) view.findViewById(R.id.newReminderDaySpinner);
        AlwaysChangeSpinner timeSpinner = (AlwaysChangeSpinner) view.findViewById(R.id.newReminderTimeSpinner);

        // setup day spin adapter
        ArrayList<String> days = new ArrayList<String>(
                Arrays.asList(getResources().getStringArray(R.array.spinner_day)));
        dayAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(String day : days)
            dayAdapter.add(day);
        daySpinner.setAdapter(dayAdapter);
        daySpinner.setOnItemSelectedListener(newReminderOnItemSelectedListener);

        // setup time spin adapter
        ArrayList<String> times = new ArrayList<String>(
                Arrays.asList(getResources().getStringArray(R.array.spinner_time)));
        timeAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(String time : times)
            timeAdapter.add(time);
        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setOnItemSelectedListener(newReminderOnItemSelectedListener);
    }

    public void initializeEdit(Reminder r){
        Log.d(TAG, "Filling in data for edit reminder");
        // Get views
        TextView descriptionTextView = (TextView)rootView.findViewById(R.id.newReminderEditText);
        Spinner daySpinner = (Spinner)rootView.findViewById(R.id.newReminderDaySpinner);
        Spinner timeSpinner = (Spinner)rootView.findViewById(R.id.newReminderTimeSpinner);
        TextView button = (TextView)rootView.findViewById(R.id.newReminderCreateButton);

        // assign values
        descriptionTextView.setText(r.getDescription());
        if(r.getDateOffset() == DATE_OTHER){
            handleNewDate(r.getYear(), r.getMonth(), r.getMonthDay());
            daySpinnerSetInCode = true;
            daySpinner.setSelection(r.getDateOffset());
        } else {
            daySpinner.setSelection(r.getDateOffset());
        }
        if(r.getTimeOffset() == TIME_OTHER){
            handleNewTime(r.getHour(), r.getMinute());
            timeSpinnerSetInCode = true;
            timeSpinner.setSelection(r.getTimeOffset());
        } else {
            timeSpinner.setSelection(r.getTimeOffset());
        }

        button.setText(R.string.edit_reminder_save);
    }


    /***
     * On Click Listeners
     */
    private View.OnClickListener newReminderOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.newReminderCreateButton:
                    Log.d(TAG, "New reminder button clicked.");
                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_saving_reminder), Toast.LENGTH_SHORT).show();
                    saveNoteAndReturn(reminderHolder);
                    break;
            }
        }
    };

    private AdapterView.OnItemSelectedListener newReminderOnItemSelectedListener
            = new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            switch(parentView.getId()) {
                case R.id.newReminderTimeSpinner:
                    // check if we put the time in
                    // if so, we don't want it to run
                    if(timeSpinnerSetInCode){
                        timeSpinnerSetInCode = false;
                        return;
                    }
                    if (position == TIME_OTHER) {
                        Log.d(TAG,"Custom time selected");
                        Calendar now = Calendar.getInstance();
                        RadialTimePickerDialog timePickerDialog;
                        // check if we are initializing it with last entries data
                        // If user selected custom time, then wants to change it, we want the picker
                        // to start with the other custom time
                        if(customTimeSelected) {
                            timePickerDialog = RadialTimePickerDialog
                                    .newInstance(NewReminderFragment.this, spinner_hour, spinner_minute,
                                            DateFormat.is24HourFormat(getActivity())
                                    );
                        } else {
                            // initialize with time right now
                            timePickerDialog = RadialTimePickerDialog
                                    .newInstance(NewReminderFragment.this, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                                            DateFormat.is24HourFormat(getActivity())
                                    );
                        }

                        if (mHasDialogFrame) {
                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

                            ft.add(R.id.content_frame, timePickerDialog, FRAG_TAG_TIME_PICKER);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            ft.commit();
                        } else {
                            timePickerDialog.show(getActivity().getSupportFragmentManager(), FRAG_TAG_TIME_PICKER);
                        }
                        customTimeSelected = true;
                    }
                    else{
                        // update spinner
                        Log.d(TAG,"Resetting the time spinner");
                        String[] times = getResources().getStringArray(R.array.spinner_time);
                        // replace last entry with default string in case this has been changes
                        timeAdapter.clear();
                        timeAdapter.addAll(times);
                        timeAdapter.notifyDataSetChanged();
                        customTimeSelected = false;
                    }
                    break;
                case R.id.newReminderDaySpinner:
                    // check if we put the date in
                    // if so, we don't want it to run
                    if(daySpinnerSetInCode){
                        daySpinnerSetInCode = false;
                        return;
                    }
                    if(position == DATE_OTHER){
                        Log.d(TAG,"Custom date selected");
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        Calendar now = Calendar.getInstance();
                        Log.d(TAG,"Initializing with "+now.get(Calendar.YEAR)+" "+now.get(Calendar.MONTH)+" "+now.get(Calendar.DAY_OF_MONTH));

                        CalendarDatePickerDialog calendarDatePickerDialog;
                        // check if we are initializing it with last entries data
                        // If user selected custom time, then wants to change it, we want the picker
                        // to start with the other custom date
                        if(customDateSelected){
                            calendarDatePickerDialog = CalendarDatePickerDialog
                                    .newInstance(NewReminderFragment.this, spinner_year, spinner_month,
                                            spinner_day);

                        } else {
                            calendarDatePickerDialog = CalendarDatePickerDialog
                                    .newInstance(NewReminderFragment.this, now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                                            now.get(Calendar.DAY_OF_MONTH));
                        }
                        calendarDatePickerDialog.show(fm, FRAG_TAG_DATE_PICKER);
                        customDateSelected = true;
                    }
                    else{
                        // update spinner
                        Log.d(TAG,"Resetting the date spinner");
                        String[] days = getResources().getStringArray(R.array.spinner_day);
                        // replace last entry with default string in case this has been changes
                        dayAdapter.clear();
                        dayAdapter.addAll(days);
                        dayAdapter.notifyDataSetChanged();
                        customDateSelected = false;
                    }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // your code here
        }
    };



    private void saveNoteAndReturn(Reminder r){

        // Get text view for description
        EditText et = (EditText)rootView.findViewById(R.id.newReminderEditText);
        String description;

        // make sure string is not null or empty
        if(et.getText() != null && !et.getText().toString().isEmpty()) {
            description = et.getText().toString();
        }
        else {
            Toast.makeText(getActivity(), getString(R.string.toast_new_reminder_no_description), Toast.LENGTH_SHORT).show();
            return;
        }
        r.setDescription(description);

        // Get Day
        Spinner sDate = (Spinner)rootView.findViewById(R.id.newReminderDaySpinner);
        int day = sDate.getSelectedItemPosition();
        r.setDateOffset(day);

        // Get Time
        Spinner sTime = (Spinner)rootView.findViewById(R.id.newReminderTimeSpinner);
        int time = sTime.getSelectedItemPosition();
        r.setTimeOffset(time);

        // build date-time string
        buildDateTimeString(r);

        // find time for reminder
        r.calculateMsTime(spinner_year, spinner_month, spinner_day, spinner_hour, spinner_minute);

        // save items
        r.setYear(spinner_year);
        r.setMonth(spinner_month);
        r.setMonthDay(spinner_day);
        r.setHour(spinner_hour);
        r.setMinute(spinner_minute);

        // Log and save reminder
        r.outputReminderToLog();
        r.writeToFile(getActivity());

        // kick off reminder
        r.setAlarm(getActivity());

        Log.d(TAG,"Reminder saved.");
        Toast.makeText(getActivity(), getString(R.string.toast_new_reminder_created), Toast.LENGTH_SHORT).show();

        // return to main View
        Bundle b = new Bundle();
        b.putInt(MainActivity.MESSAGE_TASK, MainActivity.TASK_CHANGE_FRAG);
        b.putInt(MainActivity.TASK_INT, MainActivity.REMINDER_LIST);
        messenger.send(b);

    }

    public void hideKeyboard(){

        InputMethodManager myInputMethodManager = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        EditText et = (EditText) rootView.findViewById(R.id.newReminderEditText);

        Log.d(TAG, "Hiding keyboard");
        myInputMethodManager.hideSoftInputFromWindow(et.getWindowToken(), 0);

    }

    private void buildDateTimeString(Reminder r){
        int time = r.getTimeOffset();
        String returnString = "";

        // Beginning of string
        returnString += "";

        // Add date
        returnString += dayAdapter.getItem(r.getDateOffset());

        returnString += " ";

        /**
         *  Syntactical Possibilites
         *
         *  Date at Time
         *  Date in the Morning
         *  Date at Noon
         *  Date in the Afternoon
         *  Date in the Evening
         *  Date at Night
         */
        switch (r.getTimeOffset()){
            case TIME_MORNING:
            case TIME_AFTERNOON:
            case TIME_EVENING:
                returnString += "in the";
                break;
            case TIME_NOON:
            case TIME_NIGHT:
            default:
                returnString += "at";
        }

        returnString += " ";

        returnString += timeAdapter.getItem(time);

        r.setDateTimeString(returnString);
    }


    /**
     * Date and Time Dialog handlers
     */
    @Override
    public void onDateSet(CalendarDatePickerDialog dialog, int year, int month, int day) {
        Log.d(TAG, "Custom Date Set");
        Log.d(TAG, "Year "+year+" Month "+month+" Day "+day);

        handleNewDate(year, month, day);
    }

    @Override
    public void onTimeSet(RadialPickerLayout dialog, int hour, int minute) {
        Log.d(TAG, "Custom Time Set");
        Log.d(TAG, "Hour "+hour+" Minute "+hour);

        handleNewTime(hour, minute);
    }

    private void handleNewDate(int year, int month, int day){

        // record values for spinner
        spinner_year = year;
        spinner_month = month;
        spinner_day = day;

        // Create date string
        Calendar now = Calendar.getInstance();
        now.set(year, month, day);
        String dateString = now.getDisplayName(Calendar.MONTH, Calendar.LONG, getResources().getConfiguration().locale);
        dateString += " ";
        dateString += day;
        dateString += ", ";
        dateString += year;
        Log.d(TAG,"Date: "+dateString);

        // update spinner
        String[] days = getResources().getStringArray(R.array.spinner_day);
        // replace last entry with date string
        days[days.length-1] = dateString;
        dayAdapter.clear();
        dayAdapter.addAll(days);
        dayAdapter.notifyDataSetChanged();

        return;
    }

    private void handleNewTime(int hour, int minute){

        // record values for spinner
        spinner_hour = hour;
        spinner_minute = minute;

        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, hour);
        now.set(Calendar.MINUTE, minute);

        String timeString = "";
        timeString += now.get(Calendar.HOUR);
        timeString += ":";
        timeString += String.format("%02d",now.get(Calendar.MINUTE));
        // Add AM/PM if 12 hour
        if(!DateFormat.is24HourFormat(getActivity())) {
            timeString += " ";
            if(now.get(Calendar.AM_PM) == Calendar.AM)
                timeString += getResources().getString(R.string.time_suffix_AM);
            else if(now.get(Calendar.AM_PM) == Calendar.PM)
                timeString += getResources().getString(R.string.time_suffix_PM);
        }

        Log.d(TAG,"Time: "+timeString);

        // update spinner
        String[] times = getResources().getStringArray(R.array.spinner_time);
        // replace last entry with date string
        times[times.length-1] = timeString;
        timeAdapter.clear();
        timeAdapter.addAll(times);
        timeAdapter.notifyDataSetChanged();

    }

    @Override
    public void onResume() {
        // Reattach to time-picker
        super.onResume();
        RadialTimePickerDialog rtpd = (RadialTimePickerDialog) getActivity().getSupportFragmentManager().findFragmentByTag(
                FRAG_TAG_TIME_PICKER);
        if (rtpd != null) {
            rtpd.setOnTimeSetListener(this);
        }
        // Reattach date picker
        CalendarDatePickerDialog calendarDatePickerDialog = (CalendarDatePickerDialog) getActivity().getSupportFragmentManager()
                .findFragmentByTag(FRAG_TAG_DATE_PICKER);
        if (calendarDatePickerDialog != null) {
            calendarDatePickerDialog.setOnDateSetListener(this);
        }
    }
}