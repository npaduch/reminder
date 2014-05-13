package com.npaduch.reminder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by nolanpaduch on 5/3/14.
 *
 */

// TODO: Add custom listview for current main layout

public class MainFragment extends Fragment {

    private final static String LOG = "MainFragment";

    FragmentCommunicationListener messenger;

    public Reminder reminders[];

    public MainFragment() {
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
                    + " must implement MainFragment.FragmentCommunicationListener");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        populateSampleReminders();

        ListView list = (ListView) rootView.findViewById(R.id.mainFragmentListView);
        ReminderList myListViewArrayAdapter = new ReminderList(
                getActivity(), R.id.mainFragmentListView, reminders);
        list.setAdapter(myListViewArrayAdapter);
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_add_reminder:
                Bundle b = new Bundle();
                b.putString("Task","Change Fragment");
                b.putInt("page",MainActivity.NEW_REMINDER);
                messenger.send(b);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateSampleReminders(){

        ArrayList<Reminder> remindersArray = new ArrayList<Reminder>();

        for(int i=0; i< 20; i++)
            remindersArray.add(new Reminder());

        reminders = new Reminder[remindersArray.size()];
        reminders = remindersArray.toArray(reminders);

    }

/*
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
    */
}
