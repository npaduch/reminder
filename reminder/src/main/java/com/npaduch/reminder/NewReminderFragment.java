package com.npaduch.reminder;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by nolanpaduch on 5/8/14.
 *
 * Fragment to create a new reminder.
 */

public class NewReminderFragment extends Fragment {

    public NewReminderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.new_reminder, container, false);
        //initializeSpinners(rootView);

        ReminderCardView myCard = new ReminderCardView(getActivity());
        myCard.description.setText("This is another description, purely to avoid warnings...");

        return rootView;
    }
}