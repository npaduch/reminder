package com.npaduch.reminder;

import android.app.Activity;
import android.os.AsyncTask;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;
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

    // Arguments
    public static final String LIST_TYPE = "list_type";
    public static final int LIST_PENDING = 0;
    public static final int LIST_COMPLETED = 1;

    // Tasks for ASYNC task
    private static final int ASYNC_TASK_READ_REMINDERS = 0;
    private static final int ASYNC_TASK_WRITE_REMINDERS = 1;
    private static final int ASYNC_TASK_UPDATE_REMINDER = 2;
    private static final int ASYNC_TASK_DELETE_REMINDER = 3;


    // TODO: make this a setting
    // time until delete (in ms)
    public static final int TIME_UNTIL_DELETE = 3000;

    // list type
    public int fragmentType = LIST_PENDING;


    // Global to hold te last view clicked
    public int listItemClickedOffset = 0;

    // Make global so we can call it from onClick
    ContextualUndoAdapter undoAdapter;

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

        // get fragment type
        fragmentType = getArguments().getInt(LIST_TYPE, LIST_PENDING);

        // TODO: Move this to the background
        populateReminders();

        ListView list = (ListView) rootView.findViewById(R.id.mainFragmentListView);
        if(fragmentType == LIST_PENDING) {
            myReminderListViewArrayAdapter = new ReminderList(
                    getActivity(), R.id.mainFragmentListView, MainActivity.pendingReminders, mainFragmentOnClickListener, LIST_PENDING);
        } else if(fragmentType == LIST_COMPLETED){
            myReminderListViewArrayAdapter = new ReminderList(
                    getActivity(), R.id.mainFragmentListView, MainActivity.completedReminders, mainFragmentOnClickListener, LIST_COMPLETED);
        } else {
            Log.e(TAG, "Invalid fragment type. Will create pending list.");
            myReminderListViewArrayAdapter = new ReminderList(
                    getActivity(), R.id.mainFragmentListView, MainActivity.pendingReminders, mainFragmentOnClickListener, LIST_PENDING);
        }

        // Appearance animation
        // Swing Right in and fade in
        // Stack the adapters
        AlphaInAnimationAdapter alphaInAnimationAdapter =
                new AlphaInAnimationAdapter(myReminderListViewArrayAdapter);
        SwingRightInAnimationAdapter swingRightInAnimationAdapter =
                new SwingRightInAnimationAdapter(alphaInAnimationAdapter);

        // swipe to undo adapter
        if(fragmentType == LIST_PENDING) {
            undoAdapter = new ContextualUndoAdapter(swingRightInAnimationAdapter,
                            R.layout.undo_reminder_entry,
                            R.id.undo_reminder_entry_button,
                            TIME_UNTIL_DELETE,
                            removeItem);
        } else {
            undoAdapter = new ContextualUndoAdapter(swingRightInAnimationAdapter,
                            R.layout.undo_completed_reminder_entry,
                            R.id.undo_completed_reminder_entry_button,
                            TIME_UNTIL_DELETE,
                            removeItem);
        }

        // combine all adapters and set them to the listview
        undoAdapter.setAbsListView(list);

        list.setAdapter(undoAdapter);

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
            Reminder clickedReminder;
            if(fragmentType == LIST_PENDING) {
                clickedReminder = MainActivity.pendingReminders.get(position);
            } else { // completed
                clickedReminder = MainActivity.completedReminders.get(position);
            }
            // find location within main reminder list
            listItemClickedOffset = MainActivity.reminders.indexOf(clickedReminder);
            Log.d(TAG, "Reminder to be editted:");
            clickedReminder.outputReminderToLog();

            myReminderListViewArrayAdapter.toggleExpanded(position);
            myReminderListViewArrayAdapter.notifyDataSetChanged();
            //toggleView(view);
            return;
        }
    };

    // TODO: Fix this bug.
    /**
     * 1. Click on one reminder
     * 2. Click on second
     * 3. Click on first reminder's button
     * 4. Action is executed on second reminder
     */
    private View.OnClickListener mainFragmentOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toggleView((LinearLayout)view.getParent());
            int position = (Integer)((LinearLayout)view.getParent()).getTag();
            myReminderListViewArrayAdapter.toggleExpanded(position);
            Log.d(TAG, "Parent position of click: "+position);

            switch (view.getId()) {
                case R.id.reminderEntryEdit:
                    Log.d(TAG, "Edit reminder button clicked.");
                    int mainReminderOffset = 0;
                    if(fragmentType == LIST_PENDING){
                        mainReminderOffset = MainActivity.reminders.indexOf(MainActivity.pendingReminders.get(listItemClickedOffset));
                    } else { // completed
                        mainReminderOffset = MainActivity.reminders.indexOf(MainActivity.completedReminders.get(listItemClickedOffset));
                    }
                    Bundle b = new Bundle();
                    b.putInt(MainActivity.MESSAGE_TASK, MainActivity.TASK_EDIT_REMINDER);
                    b.putInt(MainActivity.TASK_INT, mainReminderOffset);
                    b.putInt(MainActivity.TASK_INITIATOR, MainActivity.PENDING_REMINDERS);
                    messenger.send(b);
                    break;
                case R.id.reminderEntryShare:
                    Log.d(TAG, "Share reminder button clicked.");
                    break;
                case R.id.reminderEntryDismiss:
                    Log.d(TAG, "Dismiss reminder button clicked.");
                    // Call same method as if user swiped it
                    undoAdapter.swipeViewAtPosition(position);
                    break;
            }
        }
    };

    private void toggleView(View view){
        LinearLayout ll = (LinearLayout)view.findViewById(R.id.reminderExpanded);
        if(ll.getVisibility() == View.GONE){
            ll.setVisibility(View.VISIBLE);
        } else {
            ll.setVisibility(View.GONE);
        }
    }

    // DeleteItemCallback
    ContextualUndoAdapter.DeleteItemCallback removeItem = new ContextualUndoAdapter.DeleteItemCallback(){
        @Override
        public void deleteItem(int position) {
            if(fragmentType == LIST_PENDING) {
                dismissItem(position);
            } else { // completed
                permanentlyDeleteItem(position);
            }
        }
    };

    private void dismissItem(int position){
        Log.d(TAG, "Deleting item:"+position);
        // set item completed
        MainActivity.pendingReminders.get(position).setCompleted(true);
        // cancel alarm
        MainActivity.pendingReminders.get(position).cancelAlarm(getActivity());
        // make change in file
        // get index of reminder
        int index = MainActivity.pendingReminders.indexOf(myReminderListViewArrayAdapter.get(position));
        UpdateFile uf = new UpdateFile(
                ASYNC_TASK_UPDATE_REMINDER,     // save updated reminder
                MainActivity.pendingReminders.get(index), // reminder to be saved
                true // read file back into lists when done
        );
        uf.execute();
        //MainActivity.pendingReminders.get(position).writeToFile(getActivity());
        // set expanded view to hidden
        // remove it from the list view
        //myReminderListViewArrayAdapter.remove(MainActivity.pendingReminders.get(position));
        myReminderListViewArrayAdapter.remove(position);
        myReminderListViewArrayAdapter.notifyDataSetChanged();
        // sync after view is updated. We don't want to remove two items.
        // This will keep the file in line with what's currently displayed
        //MainActivity.reminders = Reminder.getJSONFileContents(getActivity());
        //MainActivity.syncReminders();
    }

    private void permanentlyDeleteItem(int position){
        Log.d(TAG, "Permanently deleting item:"+position);
        // remove from file
        // get index of reminder
        int index = MainActivity.completedReminders.indexOf(myReminderListViewArrayAdapter.get(position));
        UpdateFile uf = new UpdateFile(
                ASYNC_TASK_DELETE_REMINDER,     // delete reminder
                MainActivity.completedReminders.get(index), // reminder to be saved
                true // read file back into lists when done
        );
        uf.execute();
        // remove it from the list view
        myReminderListViewArrayAdapter.remove(position);
        myReminderListViewArrayAdapter.notifyDataSetChanged();
        // sync after view is updated. We don't want to remove two items.
        // This will keep the file in line with what's currently displayed
        //MainActivity.reminders = Reminder.getJSONFileContents(getActivity());
        //MainActivity.syncReminders();
    }

    /** Asynchronous task for reading/writing to file **/
    private class UpdateFile extends AsyncTask {

        // Task to complete in the background
        int task;
        // Reminder to manipulate (if we need to)
        Reminder reminder;
        // sync files with current status
        boolean sync;

        public UpdateFile(int task, Reminder r, boolean sync) {
            super();
            this.task = task;
            this.reminder = r;
            this.sync = sync;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            switch ( task ) {
                case ASYNC_TASK_READ_REMINDERS:
                    break;
                case ASYNC_TASK_WRITE_REMINDERS:
                    break;
                case ASYNC_TASK_UPDATE_REMINDER:
                    reminder.writeToFile(getActivity());
                    break;
                case ASYNC_TASK_DELETE_REMINDER:
                    reminder.removeFromFile(getActivity());
                    break;
            }
            if(sync){
                MainActivity.reminders = Reminder.getJSONFileContents(getActivity());
                MainActivity.syncReminders();
            }
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
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }
    }
}
