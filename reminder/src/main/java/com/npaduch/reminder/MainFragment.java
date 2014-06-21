package com.npaduch.reminder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by nolanpaduch on 5/3/14.
 *
 */

public class MainFragment extends Fragment {

    private final static String TAG = "MainFragment";

    FragmentCommunicationListener messenger;

    public static ReminderList myReminderListViewArrayAdapter;

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

        // Check for reminders in file
        ArrayList<Reminder> oldReminders = getJSONFileContents();

        populateReminders(oldReminders);


        ListView list = (ListView) rootView.findViewById(R.id.mainFragmentListView);
        myReminderListViewArrayAdapter = new ReminderList(
                getActivity(), R.id.mainFragmentListView, MainActivity.reminders);
        list.setAdapter(myReminderListViewArrayAdapter);
        list.setOnItemClickListener(listviewOnItemClickListener);

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
                b.putInt(MainActivity.MESSAGE_TASK,MainActivity.TASK_CHANGE_FRAG);
                b.putInt(MainActivity.TASK_INT,MainActivity.NEW_REMINDER);
                messenger.send(b);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void populateReminders(ArrayList<Reminder> oldReminders){

        if(MainActivity.reminders == null)
            MainActivity.reminders = new ArrayList<Reminder>();

        // TODO: Change to create samples if first time launching?
        if(oldReminders == null){
            // set it to be an array list
            oldReminders = new ArrayList<Reminder>();
        }

        // Copy reminders over to list view instance
        MainActivity.reminders.clear();
        for(int i = 0; i < oldReminders.size(); i++)
            MainActivity.reminders.add(oldReminders.get(i));

    }


    // This is different from the reminder class's instance!
    // It will create a new file if one is not found.
    private ArrayList<Reminder> getJSONFileContents(){
        Log.d(TAG, "Looking for file " + getActivity().getFilesDir() + File.pathSeparator + Reminder.filename);

        // Check for file
        File file = new File(getActivity().getFilesDir(), Reminder.filename);
        if(!file.exists()){
            // it doesn't exist. Write an empty JSON array to it
            Log.d(TAG, "Initializing JSON file.");
            try {
                Reminder.initFile(getActivity());
            } catch (Exception e){
                Log.e(TAG, "Error creating JSON file: "+e);
            }
            return null;
        }
        Log.d(TAG,"JSON file found. Loading in reminders.");

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return Reminder.readJsonStream(fileInputStream);
        } catch (Exception e){
            Log.e(TAG, "Error reading existing JSON file: "+e);
        }
        Log.e(TAG, "Could not read input stream to get existing reminders.");
        return null;
    }

    AdapterView.OnItemClickListener listviewOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Reminder clickedReminder = MainActivity.reminders.get(position);
            Log.d(TAG, "Reminder to be editted:");
            clickedReminder.outputReminderToLog();

            Bundle b = new Bundle();
            b.putInt(MainActivity.MESSAGE_TASK, MainActivity.TASK_EDIT_REMINDER);
            b.putInt(MainActivity.TASK_INT, position);
            messenger.send(b);
        }
    };

}
