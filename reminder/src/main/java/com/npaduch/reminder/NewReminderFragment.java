package com.npaduch.reminder;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.text.format.Time;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.doomonafireball.betterpickers.recurrencepicker.RecurrencePickerDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardView;


/**
 * Created by nolanpaduch on 5/8/14.
 *
 * Fragment to create a new reminder.
 */

/** To-do list for this fragment */

// TODO: Make sure time is in the future
// TODO: Recurring reminders
// TODO: Verify date + time actually selected (i.e. back button clicked on specific date)

class NewReminderFragment extends Fragment
        implements RadialTimePickerDialog.OnTimeSetListener,
        CalendarDatePickerDialog.OnDateSetListener,
        RecurrencePickerDialog.OnRecurrenceSetListener {

    // Logging
    private final static String TAG = "NewReminderFragment";

    // delay time for keyboard popup on entry
    private final static int KEYBOARD_POPUP_DELAY = 200;

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
    private static final int DATE_OTHER    = 2;

    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";
    private static final String FRAG_TAG_DATE_PICKER = "datePickerDialogFragment";
    private static final String FRAG_TAG_RECUR_PICKER = "recurrencePickerDialogFragment";

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

    // Recurrence
    private String recurrenceRule;
    private RecurringReminder mEventRecurrence;

    // Main view
    private View rootView;
    // spinners
    private ArrayAdapter<CharSequence> dayAdapter;
    private ArrayAdapter<CharSequence> timeAdapter;
    // handle the case if set by code
    private boolean daySpinnerSetInCode = false;
    private boolean timeSpinnerSetInCode = false;

    // Application Context
    private Context context;

    private Reminder reminderHolder;

    private Reminder reminderToEdit;

    private boolean isEdit = false;

    public NewReminderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "New Reminder OnCreateView");

        // inflate view first
        rootView = inflater.inflate(R.layout.new_reminder, container, false);

        // Create a Card
        Card card = new Card(getActivity(), R.layout.new_reminder_card);

        // Set card in the cardView
        CardView cardView = (CardView) rootView.findViewById(R.id.cardview_new_reminder);
        cardView.setCard(card);

        // initialize Spinners with string data
        initializeSpinners(rootView);

        // Set on click listeners
        TextView createButton = (TextView) rootView.findViewById(R.id.newReminderCreateButton);
        createButton.setOnClickListener(newReminderOnClickListener);
        CheckBox repeatCheckbox = (CheckBox) rootView.findViewById(R.id.newReminderRecurrenceCheckbox);
        repeatCheckbox.setOnClickListener(new mOnCheckClickListener());

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
        mEventRecurrence = new RecurringReminder();

        // Check if we're actually editing a note
        if(getReminderToEdit() != null) {
            initializeEdit(getReminderToEdit());
            reminderHolder = getReminderToEdit();
            isEdit = true;
        }

        // save off context
        this.context = getActivity();

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
            case R.id.action_cancel_new_reminder:
                BusEvent event = new BusEvent(BusEvent.TYPE_CHANGE_FRAG, BusEvent.TARGET_MAIN);
                event.setToFragment(BusEvent.FRAGMENT_PENDING);
                event.setFromFragment(BusEvent.FRAGMENT_NEW_REMINDER);
                BusProvider.getInstance().post(event);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    void initializeSpinners(View view){

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
        // we need to set the default after the listener is assigned
        daySpinnerSetInCode = true;
        daySpinner.setSelection(getNextDayWindow());

        // setup time spin adapter
        ArrayList<String> times = new ArrayList<String>(
                Arrays.asList(getResources().getStringArray(R.array.spinner_time)));
        timeAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(String time : times)
            timeAdapter.add(time);
        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setOnItemSelectedListener(newReminderOnItemSelectedListener);
        // we need to set the default after the listener is assigned
        timeSpinnerSetInCode = true;
        timeSpinner.setSelection(getNextTimeWindow());
    }

    void initializeEdit(Reminder r){
        Log.d(TAG, "Filling in data for edit reminder");
        // Get views
        TextView descriptionTextView = (TextView)rootView.findViewById(R.id.newReminderEditText);
        Spinner daySpinner = (Spinner)rootView.findViewById(R.id.newReminderDaySpinner);
        Spinner timeSpinner = (Spinner)rootView.findViewById(R.id.newReminderTimeSpinner);
        TextView button = (TextView)rootView.findViewById(R.id.newReminderCreateButton);

        // assign values
        descriptionTextView.setText(r.getDescription());
        if(r.getDateOffset() == DATE_OTHER){
            handleNewDate(getActivity(), r.getYear(), r.getMonth(), r.getMonthDay());
            daySpinnerSetInCode = true;
            daySpinner.setSelection(r.getDateOffset());
        } else {
            daySpinner.setSelection(r.getDateOffset());
        }
        if(r.getTimeOffset() == TIME_OTHER){
            handleNewTime(getActivity(), r.getHour(), r.getMinute());
            timeSpinnerSetInCode = true;
            timeSpinner.setSelection(r.getTimeOffset());
        } else {
            timeSpinner.setSelection(r.getTimeOffset());
        }

        // check recurrence
        RecurringReminder rr = r.getRecurrence();
        if(rr.isEnabled()){
            CheckBox cb = (CheckBox)rootView.findViewById(R.id.newReminderRecurrenceCheckbox);
            cb.setChecked(true);
            TextView recurringTextView = (TextView)rootView.findViewById(R.id.newReminderRecurrenceString);
            recurringTextView.setText(rr.makeString(getActivity()));

            TextView recurrenceString = (TextView) rootView.findViewById(R.id.newReminderRecurrenceString);

            recurrenceString.setText(rr.makeString(getActivity()));
            recurrenceString.setVisibility(View.VISIBLE);
            mEventRecurrence = rr;
        }

        button.setText(R.string.edit_reminder_save);
    }


    /***
     * On Click Listeners
     */
    private final View.OnClickListener newReminderOnClickListener = new View.OnClickListener() {
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

    private final AdapterView.OnItemSelectedListener newReminderOnItemSelectedListener
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

    private class mOnCheckClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            CheckBox cb = (CheckBox)view;
            boolean checked = cb.isChecked();
            // reset checkbox, we want to decide this!
            cb.setChecked(!checked);
            Log.d(TAG, "checked: "+checked);

            TextView recurrenceString = (TextView) rootView.findViewById(R.id.newReminderRecurrenceString);
            // Check if it wasn't checked BEFORE being clicked
            if(checked){
                // we need to spawn the recurrence picker now

                FragmentManager fm = getActivity().getSupportFragmentManager();
                Bundle b = new Bundle();
                Time time = new Time();
                time.setToNow();
                b.putLong(RecurrencePickerDialog.BUNDLE_START_TIME_MILLIS, getRecurringTime());
                b.putString(RecurrencePickerDialog.BUNDLE_TIME_ZONE, time.timezone);

                // may be more efficient to serialize and pass in EventRecurrence?
                b.putString(RecurrencePickerDialog.BUNDLE_RRULE, recurrenceRule);

                RecurrencePickerDialog rpd = (RecurrencePickerDialog) fm.findFragmentByTag(
                        FRAG_TAG_RECUR_PICKER);
                if (rpd != null) {
                    rpd.dismiss();
                }
                rpd = new RecurrencePickerDialog();
                rpd.setArguments(b);
                rpd.setOnRecurrenceSetListener(NewReminderFragment.this);
                rpd.show(fm, FRAG_TAG_RECUR_PICKER);
            } else {
                // it was checked, clear it
                cb.setChecked(false); // clear checkbox
                recurrenceString.setVisibility(View.GONE);
                mEventRecurrence.setEnabled(false);
            }
        }
    }



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

        // find time for reminder
        r.calculateMsTime(spinner_year, spinner_month, spinner_day, spinner_hour, spinner_minute);

        // save items
        r.setYear(spinner_year);
        r.setMonth(spinner_month);
        r.setMonthDay(spinner_day);
        r.setHour(spinner_hour);
        r.setMinute(spinner_minute);

        // set uncomplete
        r.setCompleted(false);

        // set recurrence (populated via handler)
        // if recurrence is count based, decrement by one now since we're setting alarm
        if(mEventRecurrence.count != 0){
            if(mEventRecurrence.count == 1){
                // in this case, make it never repeat!
                mEventRecurrence.count = RecurringReminder.FINAL_COUNT;
            }
            else {
                // just decrement and move on
                mEventRecurrence.count--;
            }
        }
        r.setRecurrence(mEventRecurrence);

        // Log and save reminder
        r.outputReminderToLog();
        SaveReminder saveReminder = new SaveReminder(r);
        saveReminder.execute();

        // kick off reminder
        r.setAlarm(getActivity());

        // return to main View
        BusEvent event = new BusEvent(BusEvent.TYPE_CHANGE_FRAG, BusEvent.TARGET_MAIN);
        event.setToFragment(BusEvent.FRAGMENT_PENDING);
        event.setFromFragment(BusEvent.FRAGMENT_NEW_REMINDER);
        BusProvider.getInstance().post(event);
    }

    public void hideKeyboard(){

        // only do this if we have actually added the fragment!
        if(!this.isAdded()){
            return;
        }

        InputMethodManager myInputMethodManager = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        EditText et = (EditText) rootView.findViewById(R.id.newReminderEditText);

        Log.d(TAG, "Hiding keyboard");
        myInputMethodManager.hideSoftInputFromWindow(et.getWindowToken(), 0);

    }


    /**
     * Date and Time Dialog handlers
     */
    @Override
    public void onDateSet(CalendarDatePickerDialog dialog, int year, int month, int day) {
        Log.d(TAG, "Custom Date Set");
        Log.d(TAG, "Year "+year+" Month "+month+" Day "+day);

        handleNewDate(getActivity(), year, month, day);
    }

    @Override
    public void onTimeSet(RadialPickerLayout dialog, int hour, int minute) {
        Log.d(TAG, "Custom Time Set");
        Log.d(TAG, "Hour "+hour+" Minute "+hour);

        handleNewTime(getActivity(), hour, minute);
    }

    @Override
    public void onRecurrenceSet(String rrule) {
        recurrenceRule = rrule;
        CheckBox cb = (CheckBox)rootView.findViewById(R.id.newReminderRecurrenceCheckbox);
        if (recurrenceRule != null) {
            mEventRecurrence.parse(recurrenceRule);
            cb.setChecked(true);
        } else {
            cb.setChecked(false);
            return;
        }
        Log.d(TAG, mEventRecurrence.toString());
        String s = mEventRecurrence.makeString(getActivity());
        Log.d(TAG, s);

        mEventRecurrence.setEnabled(true);

        TextView recurrenceString = (TextView) rootView.findViewById(R.id.newReminderRecurrenceString);

        recurrenceString.setText(s);
        recurrenceString.setVisibility(View.VISIBLE);
    }

    private void handleNewDate(Context context, int year, int month, int day){

        // record values for spinner
        spinner_year = year;
        spinner_month = month;
        spinner_day = day;

        // Create date string
        Calendar now = Calendar.getInstance();
        now.set(year, month, day);
        String dateString = Reminder.buildDateString(context, now);
        Log.d(TAG,"Date: "+dateString);

        // update spinner
        String[] days = getResources().getStringArray(R.array.spinner_day);
        // replace last entry with date string
        days[days.length-1] = dateString;
        dayAdapter.clear();
        dayAdapter.addAll(days);
        dayAdapter.notifyDataSetChanged();
    }

    private void handleNewTime(Context context, int hour, int minute){

        // record values for spinner
        spinner_hour = hour;
        spinner_minute = minute;

        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, hour);
        now.set(Calendar.MINUTE, minute);
        String timeString = Reminder.buildTimeString(context, now);
        Log.d(TAG,"Time: "+timeString);

        // update spinner
        String[] times = getResources().getStringArray(R.array.spinner_time);
        // replace last entry with date string
        times[times.length-1] = timeString;
        timeAdapter.clear();
        timeAdapter.addAll(times);
        timeAdapter.notifyDataSetChanged();
    }

    int getNextTimeWindow() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < Reminder.TIME_MORNING_HOUR)
            return TIME_MORNING;
        else if (hour < Reminder.TIME_NOON_HOUR)
            return TIME_NOON;
        else if (hour < Reminder.TIME_AFTERNOON_HOUR)
            return TIME_AFTERNOON;
        else if (hour < Reminder.TIME_EVENING_HOUR)
            return TIME_EVENING;
        else if (hour < Reminder.TIME_NIGHT_HOUR)
            return TIME_NIGHT;
        // default to tomorrow morning
        else
            return TIME_MORNING;
    }

    int getNextDayWindow() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < Reminder.TIME_NIGHT_HOUR)
            return DATE_TODAY;
        else
            return DATE_TOMORROW;
    }

    private long getRecurringTime(){

        Reminder temp = new Reminder();

        // Get Day
        Spinner sDate = (Spinner)rootView.findViewById(R.id.newReminderDaySpinner);
        int day = sDate.getSelectedItemPosition();
        temp.setDateOffset(day);

        // Get Time
        Spinner sTime = (Spinner)rootView.findViewById(R.id.newReminderTimeSpinner);
        int time = sTime.getSelectedItemPosition();
        temp.setTimeOffset(time);

        // find time for reminder
        temp.calculateMsTime(spinner_year, spinner_month, spinner_day, spinner_hour, spinner_minute);

        return temp.getMsTime();
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
        // Reattach recurrence picket
        RecurrencePickerDialog rpd = (RecurrencePickerDialog) getActivity().getSupportFragmentManager().findFragmentByTag(
                FRAG_TAG_RECUR_PICKER);
        if (rpd != null) {
            rpd.setOnRecurrenceSetListener(this);
        }

        // set title
        if(isEdit) {
            getActivity().getActionBar().setTitle(R.string.edit_title);
        } else {
            getActivity().getActionBar().setTitle(R.string.new_title);
        }
        // get event bus
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // release event bus
        BusProvider.getInstance().unregister(this);
    }


    /** Asynchronous task for reading/writing to file **/
    private class SaveReminder extends AsyncTask {

        // Reminder to save
        final Reminder reminder;

        public SaveReminder(Reminder r) {
            super();
            this.reminder = r;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            reminder.writeToFile(context);

            MainActivity.reminders = Reminder.getJSONFileContents(context);
            MainActivity.syncReminders();

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
            Toast.makeText(context, R.string.toast_new_reminder_created, Toast.LENGTH_SHORT).show();
            BusProvider.getInstance().post(new BusEvent(BusEvent.TYPE_ADD, BusEvent.TARGET_PENDING, reminder));
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }
    }

    public Reminder getReminderToEdit() {
        return reminderToEdit;
    }

    public void setReminderToEdit(Reminder reminderToEdit) {
        this.reminderToEdit = reminderToEdit;
    }
}