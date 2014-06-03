package com.npaduch.reminder;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by nolanpaduch on 5/8/14.
 *
 * Fragment to create a new reminder.
 */

public class NewReminderFragment extends Fragment {

    // Logging
    public final static String TAG = "NewReminderFragment";

    // Communication with main activity
    FragmentCommunicationListener messenger;

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

        // Set focus on edit text
        EditText et = (EditText) rootView.findViewById(R.id.newReminderEditText);
        et.requestFocus();
        // force keyboard to open
        showKeyboard(true);

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
                showKeyboard(false);
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
        String day = (String)sDate.getSelectedItem();
        r.setDateString(day);

        // Get Time
        Spinner sTime = (Spinner)rootView.findViewById(R.id.newReminderTimeSpinner);
        String time = (String)sTime.getSelectedItem();
        r.setTimeString(time);

        // Save reminder
        Log.d(TAG,"Description: "+r.getDescription());
        Log.d(TAG,"Date: "+r.getDateString());
        Log.d(TAG,"Time: "+r.getTimeString());
        MainActivity.reminders.add(0, r);

        // Hide soft keyboard
        showKeyboard(false);

        Log.d(TAG,"Note saved.");
        Toast.makeText(getActivity(), getString(R.string.new_reminder_created), Toast.LENGTH_SHORT).show();

        // return to main View
        Bundle b = new Bundle();
        b.putString("Task","Change Fragment");
        b.putInt("page",MainActivity.REMINDER_LIST);
        messenger.send(b);

    }

    public void showKeyboard(boolean show){

        InputMethodManager myInputMethodManager = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        // Show the keyboard
        if(show) {
            myInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
        // hide the keyboard
        else {
            myInputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

}