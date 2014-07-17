package com.npaduch.reminder;


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
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 *
 * Created by nolanpaduch on 5/3/14.
 *
 * Main Activity to spawn necessary fragments
 */

// TODO: set fragment tags
// TODO: Replace frag communication with a BUS

public class MainActivity extends FragmentActivity {

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
    NewReminderFragment newReminderFragment;
    public static CardListFragment pendingFragment;
    public static CardListFragment completedFragment;
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
            currentFragment = BusEvent.FRAGMENT_PENDING;
            changeFragment(BusEvent.FRAGMENT_PENDING, currentFragment, false);
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
        // Set color
        tintManager.setTintColor(getResources().getColor(R.color.app_color_theme));

        // set to enable drawer from action bar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // check if this was called from a notification
        // if it was, set the reminder to ALL DONE
        checkIfNotification();

    }

    // TODO: Move this to background
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
        Reminder r = Reminder.findReminder( reminderId, reminders);
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

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
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
            changeFragment(BusEvent.FRAGMENT_NEW_REMINDER, currentFragment, false);
        }
        else if(position == PENDING_REMINDERS_TITLE) {
            changeFragment(BusEvent.FRAGMENT_PENDING, currentFragment, false);
        }
        else if(position == COMPLETED_REMINDERS_TITLE) {
            changeFragment(BusEvent.FRAGMENT_COMPLETED, currentFragment, false);
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);

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

    public void changeFragment(int fragmentTo, int fragmentFrom, boolean resetNewFragment){

        Fragment fragment;  // holder for fragment

        // 1. Init frag if neccessary, set title, set fragment
        switch (fragmentTo) {
            case BusEvent.FRAGMENT_NEW_REMINDER:
                if (newReminderFragment == null) {
                    newReminderFragment = new NewReminderFragment();
                }
                // Check whether to set to New or Edit
                if(newReminderFragment.getReminderToEdit() != null) {
                    setTitle(getResources().getString(R.string.edit_title));
                } else{
                    setTitle(getResources().getStringArray(R.array.drawer_titles)[NEW_REMINDER_TITLE]);
                }
                fragment = newReminderFragment;
                break;
            case BusEvent.FRAGMENT_COMPLETED:
                if (completedFragment == null) {
                    initCompletedFragment();
                }
                setTitle(getResources().getStringArray(R.array.drawer_titles)[COMPLETED_REMINDERS_TITLE]);
                fragment = completedFragment;
                break;
            case BusEvent.FRAGMENT_PENDING:
            default:  // default to pending
                if (pendingFragment == null) {
                    initPendingFragment();
                }
                setTitle(R.string.app_name);
                fragment = pendingFragment;
                break;
        }

        // 2. Check if we need to hide the keyboard
        if(fragmentFrom == BusEvent.FRAGMENT_NEW_REMINDER){
            if(newReminderFragment == null){
                Log.e(TAG, "NewReminderFragment is null. This should never happen.");
            }
            else{
                newReminderFragment.hideKeyboard();
            }
        }

        // 3. Create fragment and set transition
        // Insert the fragment by replacing any existing fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(fragmentTo == BusEvent.FRAGMENT_NEW_REMINDER)
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        else if((fragmentTo == BusEvent.FRAGMENT_PENDING) && (fragmentFrom == BusEvent.TARGET_COMPLETED))
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        else if((fragmentTo == BusEvent.FRAGMENT_PENDING) && (fragmentFrom == BusEvent.FRAGMENT_COMPLETED))
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        else if(fragmentTo == BusEvent.FRAGMENT_PENDING) // activity start
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        else if(fragmentTo == BusEvent.FRAGMENT_COMPLETED)
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        else
            Log.e(TAG, "Invalid fragment tyoe. This should never happen...");

        // 4. Commit fragment to transition

        ft.replace(R.id.content_frame, fragment, FRAGMENT_TAG);
        ft.addToBackStack(null); // handle back button
        ft.commit();

        // 5. keep track of current fragment
        currentFragment = fragmentTo;

        // 6. Reset New Fragment if requested and we're not switching to it
        if(resetNewFragment && fragmentTo != BusEvent.FRAGMENT_NEW_REMINDER)
            newReminderFragment = null;
    }

    @Override
    public void onBackPressed() {
        if(currentFragment == BusEvent.FRAGMENT_NEW_REMINDER){
            changeFragment(BusEvent.FRAGMENT_PENDING, BusEvent.FRAGMENT_NEW_REMINDER, false);
        }
        else{
            super.onBackPressed();
        }
    }

    private void initPendingFragment(){
        pendingFragment = new CardListFragment();
        Bundle args = new Bundle();
        args.putInt(CardListFragment.LIST_TYPE, CardListFragment.LIST_PENDING);
        pendingFragment.setArguments(args);
    }

    private void initCompletedFragment(){
        completedFragment = new CardListFragment();
        Bundle args = new Bundle();
        args.putInt(CardListFragment.LIST_TYPE, CardListFragment.LIST_COMPLETED);
        completedFragment.setArguments(args);
    }

    // prepare to change fragment to new fragemnt
    // pre-populate fields first
    public void handleEditReminder(Reminder r){
        // reinitialize so we can fill with edit data
        newReminderFragment = new NewReminderFragment();
        newReminderFragment.setReminderToEdit(r);
        changeFragment(BusEvent.FRAGMENT_NEW_REMINDER, currentFragment, false);
    }

    /** Event bus listener **/
    @Subscribe
    public void BusEvent(BusEvent event){
        // check if it's for us
        // TODO: compare for pending or completed
        if(!event.getTargets().contains(BusEvent.TARGET_MAIN))
            return;
        Log.d(TAG, "Message received: " + event.getType());
        switch(event.getType()){
            case BusEvent.TYPE_CHANGE_FRAG:
                if(event.getToFragment() == BusEvent.FRAGMENT_NEW_REMINDER)
                    changeFragment(event.getToFragment(), event.getFromFragment(), false);
                else
                    changeFragment(event.getToFragment(), event.getFromFragment(), true);
                break;
            case BusEvent.TYPE_EDIT_REMINDER:
                handleEditReminder(event.getReminder());
                break;
        }
    }

}
