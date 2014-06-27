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
import android.widget.RelativeLayout;

import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingRightInAnimationAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by nolanpaduch on 5/3/14.
 *
 */

// TODO: Add samples if first time launching

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

        populateReminders();

        ListView list = (ListView) rootView.findViewById(R.id.mainFragmentListView);
        myReminderListViewArrayAdapter = new ReminderList(
                getActivity(), R.id.mainFragmentListView, MainActivity.pendingReminders);

        // Appearance animation
        // Swing Right in and fade in
        AlphaInAnimationAdapter alphaInAnimationAdapter =
                new AlphaInAnimationAdapter(myReminderListViewArrayAdapter);
        SwingRightInAnimationAdapter swingRightInAnimationAdapter =
                new SwingRightInAnimationAdapter(alphaInAnimationAdapter);
        // Assign the ListView to the AnimationAdapter and vice versa
        swingRightInAnimationAdapter.setAbsListView(list);
        list.setAdapter(swingRightInAnimationAdapter);

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
                b.putInt(MainActivity.MESSAGE_TASK, MainActivity.TASK_CHANGE_FRAG);
                b.putInt(MainActivity.TASK_INT, MainActivity.NEW_REMINDER);
                messenger.send(b);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void populateReminders(){

        // Check for reminders in file
        MainActivity.reminders = getJSONFileContents();

        if(MainActivity.reminders == null)
            MainActivity.reminders = new ArrayList<Reminder>();

        // Get all the pending reminders
        MainActivity.syncReminders();

    }


    /** This is different from the reminder class's instance!
     *  It will create a new file if one is not found.
     */
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
            Reminder clickedReminder = MainActivity.pendingReminders.get(position);
            // find location within main reminder list
            int offset = MainActivity.reminders.indexOf(clickedReminder);
            Log.d(TAG, "Reminder to be editted:");
            clickedReminder.outputReminderToLog();

            Bundle b = new Bundle();
            b.putInt(MainActivity.MESSAGE_TASK, MainActivity.TASK_EDIT_REMINDER);
            b.putInt(MainActivity.TASK_INT, offset);
            b.putInt(MainActivity.TASK_INITIATOR, MainActivity.PENDING_REMINDERS);
            messenger.send(b);
        }
    };

}
