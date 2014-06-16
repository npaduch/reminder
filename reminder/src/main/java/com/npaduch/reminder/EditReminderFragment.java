package com.npaduch.reminder;

import android.support.v4.app.Fragment;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;

/**
 * Created by nolanpaduch on 6/14/14.
 */
public class EditReminderFragment extends Fragment
        implements RadialTimePickerDialog.OnTimeSetListener,
        CalendarDatePickerDialog.OnDateSetListener {


    @Override
    public void onDateSet(CalendarDatePickerDialog dialog, int year, int month, int day){

    }

    @Override
    public void onTimeSet(RadialPickerLayout dialog, int hour, int minute) {

    }
}
