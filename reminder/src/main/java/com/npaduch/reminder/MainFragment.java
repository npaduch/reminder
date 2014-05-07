package com.npaduch.reminder;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by nolanpaduch on 5/3/14.
 *
 */

public class MainFragment extends Fragment {

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        initializeSpinners(rootView);
        return rootView;
    }


    public  void initializeSpinners(View view){

        // locate spinners
        Spinner daySpinner = (Spinner) view.findViewById(R.id.newNoteDaySpinner);
        Spinner timeSpinner = (Spinner) view.findViewById(R.id.newNoteTimeSpinner);

        // setup day spin adapter
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.spinner_day, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        // setup time spin adapter
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.spinner_time, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
    }
}
