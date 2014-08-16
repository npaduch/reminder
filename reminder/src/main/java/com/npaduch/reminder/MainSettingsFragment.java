package com.npaduch.reminder;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

/**
 * Created by nolanpaduch on 8/14/14.
 *
 * Fragment to display the main settings for the app
 */
//public class MainSettingsFragment extends PreferenceFragment {
public class MainSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.main_preferences);
    }

}
