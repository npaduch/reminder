package com.npaduch.reminder;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;

/**
 *
 * Created by nolanpaduch on 5/3/14.
 *
 * Main Activity to spawn necessary fragments
 */


public class MainActivity extends FragmentActivity
        implements MainFragment.FragmentCommunicationListener,
        NewReminderFragment.FragmentCommunicationListener,
        CardListFragment.FragmentCommunicationListener {

    // Debugging attributes
    String TAG = "MainActivity";

    // Drawer Label Offsets
    public static int NEW_REMINDER_TITLE = 0;
    public static int PENDING_REMINDERS_TITLE = 1;
    public static int COMPLETED_REMINDERS_TITLE = 2;
    public static int SETTINGS_TITLE = 3;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    public CharSequence mTitle;
    public CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    /* Fragment Transitions */
    public static final int REMINDER_LIST = 0;
    public static final int NEW_REMINDER = 1;
    public static final int COMPLETED_REMINDER_FRAG = 2;
    public static final String FRAGMENT_TAG = "FRAGMENT_TAG"; // handle back button

    // Holders for fragments to preserve state
    MainFragment mainFragment;
    NewReminderFragment newReminderFragment;
    MainFragment completedFragment;
    CardListFragment pendingFragment;
    public int currentFragment; // keep track of what we currently are

    // Message Passing (keys = String, values = int)
    public static final String  MESSAGE_TASK = "Task";
    public static final int TASK_CHANGE_FRAG = 10;
    public static final int TASK_EDIT_REMINDER = 11;
    public static final String TASK_INT = "int";
    public static final String TASK_INITIATOR = "initiator";
    public static final int PENDING_REMINDERS = 20;
    public static final int COMPLETED_REMINDERS = 21;

    // Reminders
    public static ArrayList<Reminder> reminders;
    // pending
    public static ArrayList<Reminder> pendingReminders;
    // completed
    public static ArrayList<Reminder> completedReminders;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Navigation Drawer */
        String[] mDrawerLabels = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerLabels));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        /* Fragment Manager */
        // load initial fragment
        if (savedInstanceState == null) {
            //initMainFragment();
            //changeFragment(mainFragment, REMINDER_LIST, false);
            initPendingFragment();
            changeFragment(pendingFragment, REMINDER_LIST, false);
        }

        /** Handle open and close drawer events */
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
                getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                // Hide keyboard if it was shown
                if(newReminderFragment != null)
                    newReminderFragment.hideKeyboard();
                getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        /** Handle Status Bar Tint */
        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        //tintManager.setNavigationBarTintEnabled(true);// set a custom tint color for all system bars
        // Set color
        tintManager.setTintColor(getResources().getColor(R.color.app_color_theme));

        // set to enable drawer from action bar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // check if this was called from a notification
        // if it was, set the reminder to ALL DONE
        checkIfNotification();

    }

    private void checkIfNotification(){
        int reminderId = getIntent().getIntExtra(
                Reminder.INTENT_REMINDER_ID,
                Reminder.BAD_REMINDER_ID);
        if(reminderId == Reminder.BAD_REMINDER_ID){
            // this wasn't called started from a notification
            return;
        }

        Log.d(TAG, "Activity opened from notification");

        // Load in reminders
        ArrayList<Reminder> reminders = Reminder.getJSONFileContents(getApplicationContext());
        if(reminders == null){
            Log.e(TAG, "Reminder list null, can't set reminder to complete.");
            return;
        }

        // find reminder
        Reminder r = Reminder.findReminder(
                reminderId,
                reminders);
        if(r == null){
            Log.e(TAG, "Couldn't find reminder. Can't set reminder to complete.");
            return;
        }

        Log.d(TAG,"Setting reminder to completed");
        r.setCompleted(true);
        r.writeToFile(getApplicationContext());

    }

    /** Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        for(int i = 0; i < menu.size(); i++)
            menu.getItem(i).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Navigation Drawer Item Click Listener */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // change to correct fragment
        if(position == NEW_REMINDER_TITLE) {
            // re-initialize in case it was cancelled last time
            if(newReminderFragment == null){
                newReminderFragment = new NewReminderFragment();
            }
            changeFragment(newReminderFragment, NEW_REMINDER, false);
            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(getResources().getStringArray(R.array.drawer_titles)[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
        else if(position == PENDING_REMINDERS_TITLE) {
            if(mainFragment == null){
                initMainFragment();
            }
            changeFragment(mainFragment, REMINDER_LIST, false);
            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(getString(R.string.app_name));
            mDrawerLayout.closeDrawer(mDrawerList);
        }
        else if(position == COMPLETED_REMINDERS_TITLE) {
            if(completedFragment == null){
                initCompletedFragment();
            }
            changeFragment(completedFragment, COMPLETED_REMINDER_FRAG, false);
            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(getString(R.string.completed_title));
            mDrawerLayout.closeDrawer(mDrawerList);
        }

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /** Navigation Draw */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

   /** Handle Communication between fragments */
   // Handle communication from other fragments
   public void send(Bundle bundle) {
       int task = bundle.getInt(MESSAGE_TASK);
       Log.d(TAG, "Task Received: " + task);
       switch(task) {
           case TASK_CHANGE_FRAG:
               handleChangeFragment(bundle);
               break;
           case TASK_EDIT_REMINDER:
               handleEditReminder(bundle);
               break;
       }
   }

    // prepare to change fragment
    public void handleChangeFragment(Bundle bundle){
        int value = bundle.getInt(TASK_INT);
        switch (value) {
            case NEW_REMINDER:
                if (newReminderFragment == null) {
                    newReminderFragment = new NewReminderFragment();
                }
                changeFragment(newReminderFragment, NEW_REMINDER, false);
                Log.d(TAG, "Setting title to new reminder title");
                setTitle(getResources().getStringArray(R.array.drawer_titles)[NEW_REMINDER_TITLE]);
                break;
            case REMINDER_LIST:
                if (mainFragment == null) {
                    initMainFragment();
                }
                changeFragment(mainFragment, REMINDER_LIST, true);
                Log.d(TAG, "Setting title to app name");
                setTitle(R.string.app_name);
                break;
        }

    }

    // prepare to change fragment to new fragemnt
    // pre-populate fields first
    public void handleEditReminder(Bundle bundle){
        // bundle contains reminder offset to edit
        int reminderOffset = bundle.getInt(TASK_INT);
        // reinitialize so we can fill with edit data
        newReminderFragment = new NewReminderFragment();
        Bundle args = new Bundle();
        args.putInt(NewReminderFragment.REMINDER_OFFSET, reminderOffset);
        newReminderFragment.setArguments(args);
        changeFragment(newReminderFragment, NEW_REMINDER, false);
        Log.d(TAG, "Setting title to edit title");
        setTitle(getResources().getString(R.string.edit_title));
    }

    /**
     * Keep reminder lists in sync
     */
    public static void syncReminders(){
        // make sure they've been initialized
        if(pendingReminders == null)
            pendingReminders = new ArrayList<Reminder>();
        if(completedReminders == null)
            completedReminders = new ArrayList<Reminder>();

        // clear the lists
        pendingReminders.clear();
        completedReminders.clear();

        // populate lists
        for(Reminder r : reminders){
            if(r.isCompleted()){
                completedReminders.add(r);
            } else {
                pendingReminders.add(r);
            }
        }
    }

    /**
     * Change fragment
     * fragment - fragment to change to
     * fragmentType - type of new fragment
     */
    public void changeFragment(Fragment fragment, int fragmentType, boolean nullifyNewReminderFragment){
        if(currentFragment == NEW_REMINDER){
            if(newReminderFragment == null){
                Log.e(TAG, "NewReminderFragment is null. This should never happen.");
            }
            else{
                newReminderFragment.hideKeyboard();
            }
        }
        // Start next new reminder from scratch if requested
        if(nullifyNewReminderFragment){
            newReminderFragment = null;
        }
        // Insert the fragment by replacing any existing fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(fragmentType == NEW_REMINDER)
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        else if((fragmentType == REMINDER_LIST) && (currentFragment == NEW_REMINDER))
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        else if((fragmentType == REMINDER_LIST) && (currentFragment == COMPLETED_REMINDER_FRAG))
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        else if(fragmentType == REMINDER_LIST) // activity start
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        else if(fragmentType == COMPLETED_REMINDER_FRAG)
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        else
            Log.e(TAG, "Invalid fragment tyoe. This should never happen...");

        ft.replace(R.id.content_frame, fragment, FRAGMENT_TAG);
        ft.addToBackStack(null); // handle back button
        ft.commit();
        currentFragment = fragmentType;
    }

    @Override
    public void onBackPressed() {
        if(currentFragment == NEW_REMINDER){
            if(mainFragment == null){
                initMainFragment();
            }
            changeFragment(mainFragment, REMINDER_LIST, false);
            setTitle(getResources().getString(R.string.app_name));
        }
        else{
            super.onBackPressed();
        }
    }

    private void initMainFragment(){
        mainFragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(MainFragment.LIST_TYPE, MainFragment.LIST_PENDING);
        mainFragment.setArguments(args);
    }

    private void initPendingFragment(){
        pendingFragment = new CardListFragment();
        Bundle args = new Bundle();
        args.putInt(MainFragment.LIST_TYPE, MainFragment.LIST_PENDING);
        pendingFragment.setArguments(args);
    }

    private void initCompletedFragment(){
        completedFragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(MainFragment.LIST_TYPE, MainFragment.LIST_COMPLETED);
        completedFragment.setArguments(args);
    }

}
