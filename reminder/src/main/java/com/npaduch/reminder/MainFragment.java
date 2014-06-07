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
import android.widget.ListView;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by nolanpaduch on 5/3/14.
 *
 */

// TODO: Add custom listview for current main layout

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

        // Check for file
        getJSONFileContents();

        populateSampleReminders();

        ListView list = (ListView) rootView.findViewById(R.id.mainFragmentListView);
        myReminderListViewArrayAdapter = new ReminderList(
                getActivity(), R.id.mainFragmentListView, MainActivity.reminders);
        list.setAdapter(myReminderListViewArrayAdapter);
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

        if(MainActivity.reminders == null)
            MainActivity.reminders = new ArrayList<Reminder>();

        for(int i=0; i< 20; i++)
            MainActivity.reminders.add(new Reminder());

    }

    private void getJSONFileContents(){
        Log.d(TAG, "CLooking for file " + getActivity().getFilesDir() + File.pathSeparator + Reminder.filename);

        // Check for file
        File file = new File(getActivity().getFilesDir(), Reminder.filename);
        if(!file.exists()){
            // it doesn't exist. Write an empty JSON array to it
            Log.d(TAG, "Initializing JSON file.");
            JSONArray jsonArray = new JSONArray();
            String s = jsonArray.toString();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(s.getBytes());
                fileOutputStream.close();
            } catch (Exception e){
                Log.e(TAG, "Error creating JSON file: "+e);
            }
            return;
        }
        Log.d(TAG,"JSON file found. Loading in reminders.");

    }


}
