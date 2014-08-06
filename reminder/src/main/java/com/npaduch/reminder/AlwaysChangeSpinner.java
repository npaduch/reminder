package com.npaduch.reminder;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Spinner;

/**
 *  Custom Spinner class
 *  Allows "onItemSelectedListener() to be called
 *  Even if value selected hasn't changed.
 *
 *  This is needed for editing a custom date or time
 */
public class AlwaysChangeSpinner extends Spinner {

    private final static String TAG = "AlwaysChangeSpinner";


    private OnItemSelectedListener listener;

    public AlwaysChangeSpinner(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void setSelection(int position)
    {
        super.setSelection(position);

        Log.d(TAG, "New Position: "+position);
        Log.d(TAG, "Old Position: "+getSelectedItemPosition());

        if (position == getSelectedItemPosition())
        {
            listener.onItemSelected(this, getSelectedView(), position, 0);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener)
    {
        this.listener = listener;
    }

}