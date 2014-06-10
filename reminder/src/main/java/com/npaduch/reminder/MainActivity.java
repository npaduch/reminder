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

import java.util.ArrayList;

/**
 *
 * Created by nolanpaduch on 5/3/14.
 *
 * Main Activity to spawn necessary fragments
 */


public class MainActivity extends FragmentActivity
        implements MainFragment.FragmentCommunicationListener,
        NewReminderFragment.FragmentCommunicationListener  {

    // Debugging attributes
    String TAG = "MainActivity";

    // Drawer Label Offsets
    public static int NEW_REMINDER_TITLE = 0;
    /* Comment these out until we need them.
    public static int ALL_REMINDERS_TITLE = 1;
    public static int TIMER_TITLE = 2;
    public static int SETTINGS_TITLE = 3;
    */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    public CharSequence mTitle;
    public CharSequence mDrawerTitle;
    private ActionBarDrawerToggle mDrawerToggle;

    /* Fragment Transitions */
    public static final int REMINDER_LIST = 0;
    public static final int NEW_REMINDER = 1;
    public static final String FRAGMENT_TAG = "FRAGMENT_TAG"; // handle back button

    // Holders for fragments to preserve state
    MainFragment mainFragment;
    NewReminderFragment newReminderFragment;
    public int currentFragment; // keep track of what we currently are

    // Reminders
    public static ArrayList<Reminder> reminders;


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
            mainFragment = new MainFragment();
            changeFragment(mainFragment, REMINDER_LIST, false);
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
        else {
            changeFragment(mainFragment, REMINDER_LIST, false);
            // Highlight the selected item, update the title, and close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(getString(R.string.app_name));
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
       Log.d(TAG, "Task Received: " + bundle.getString("Task"));
       int value = bundle.getInt("page");
       switch(value){
           case NEW_REMINDER:
               if(newReminderFragment == null){
                   newReminderFragment = new NewReminderFragment();
               }
               changeFragment(newReminderFragment, NEW_REMINDER, false);
               Log.d(TAG, "Setting title to new reminder title");
               setTitle(getResources().getStringArray(R.array.drawer_titles)[NEW_REMINDER_TITLE]);
               break;
           case REMINDER_LIST:
               if(mainFragment == null){
                   mainFragment = new MainFragment();
               }
               changeFragment(mainFragment, REMINDER_LIST, true);
               Log.d(TAG, "Setting title to app name");
               setTitle(R.string.app_name);
               break;
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
        else if(fragmentType == REMINDER_LIST)
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        else
            return; // do nothing
        ft.replace(R.id.content_frame, fragment, FRAGMENT_TAG);
        ft.addToBackStack(null); // handle back button
        ft.commit();
        currentFragment = fragmentType;
    }

    @Override
    public void onBackPressed() {
        if(currentFragment == NEW_REMINDER){
            if(mainFragment == null){
                mainFragment = new MainFragment();
            }
            changeFragment(mainFragment, REMINDER_LIST, false);
        }
        else{
            super.onBackPressed();
        }
    }

}
