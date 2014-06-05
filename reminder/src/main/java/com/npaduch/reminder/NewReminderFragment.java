package com.npaduch.reminder;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;


/**
 * Created by nolanpaduch on 5/8/14.
 *
 * Fragment to create a new reminder.
 */

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


    // Communication with main activity
    FragmentCommunicationListener messenger;
    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";

    // Keep track of time picker state
    private boolean mHasDialogFrame;


    // Main view
    View rootView;

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
        rootView = inflater.inflate(R.layout.new_reminder, container, false);

        ReminderCardView myCard = new ReminderCardView(getActivity());
        myCard.description.setText("This is another description, purely to avoid warnings...");

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
                b.putString("Task","Change Fragment");
                b.putInt("page",MainActivity.REMINDER_LIST);
                messenger.send(b);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public  void initializeSpinners(View view){

        // locate spinners
        Spinner daySpinner = (Spinner) view.findViewById(R.id.newReminderDaySpinner);
        Spinner timeSpinner = (Spinner) view.findViewById(R.id.newReminderTimeSpinner);

        // setup day spin adapter
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.spinner_day, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        // setup time spin adapter
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.spinner_time, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setOnItemSelectedListener(newReminderOnItemSelectedListener);
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
                    saveNoteAndReturn();
                    break;
            }
        }
    };

    private AdapterView.OnItemSelectedListener newReminderOnItemSelectedListener
            = new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            Log.d(TAG, "Item selected");
            switch(parentView.getId()) {
                case R.id.newReminderTimeSpinner:
                    Log.d(TAG, "In spinner");
                    if (position == TIME_OTHER) {
                        Log.d(TAG,"Item selected");
                        Time now = new Time();
                        now.setToNow();
                        RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                                .newInstance(NewReminderFragment.this, now.hour, now.minute,
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
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // your code here
        }
    };



    private void saveNoteAndReturn(){
        Reminder r = new Reminder();

        // Get text view for description
        EditText et = (EditText)rootView.findViewById(R.id.newReminderEditText);
        String description;

        // make sure string is not null or empty
        if(et.getText() != null && !et.getText().toString().isEmpty()) {
            description = et.getText().toString();
        }
        else {
            Toast.makeText(getActivity(), getString(R.string.new_reminder_no_description), Toast.LENGTH_SHORT).show();
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

        // Save reminder
        Log.d(TAG,"Description: "+r.getDescription());
        Log.d(TAG,"Date: "+r.getDateString());
        Log.d(TAG,"Time: "+r.getTimeString());
        MainActivity.reminders.add(0, r);

        Log.d(TAG,"Note saved.");
        Toast.makeText(getActivity(), getString(R.string.new_reminder_created), Toast.LENGTH_SHORT).show();

        // return to main View
        Bundle b = new Bundle();
        b.putString("Task","Change Fragment");
        b.putInt("page",MainActivity.REMINDER_LIST);
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
        int date = r.getDateOffset();
        int time = r.getTimeOffset();
        String returnString = "";
        String[] dateArray = getResources().getStringArray(R.array.spinner_day);
        String[] timeArray = getResources().getStringArray(R.array.spinner_time);

        // Beginning of string
        returnString += "";

        // Add date
        returnString += dateArray[date];

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
        if( (time == TIME_MORNING) ||
            (time == TIME_AFTERNOON) ||
            (time == TIME_EVENING) ){
            returnString += "in the";
        }
        else if((time == TIME_NOON) || (time == TIME_NIGHT)) {
            returnString += "at";
        }
        else{
            // Specific Time
            returnString += "at";
        }

        returnString += " ";

        returnString += timeArray[time];

        r.setDateTimeString(returnString);
    }


    /**
     * Date and Time Dialog handlers
     */
    @Override
    public void onDateSet(CalendarDatePickerDialog dialog, int year, int month, int day) {
        Log.d(TAG, "Custom Date Set");
        Log.d(TAG, "Year "+year+" Month "+month+" Day "+day);
    }

    @Override
    public void onTimeSet(RadialPickerLayout dialog, int hour, int minute) {
        Log.d(TAG, "Custom Time Set");
        Log.d(TAG, "Hour "+hour+" Minute "+hour);
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
    }

}