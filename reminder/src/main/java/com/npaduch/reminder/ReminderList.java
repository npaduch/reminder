package com.npaduch.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nolanpaduch on 5/12/14.
 *
 * Array adapter for the list view
 */
public class ReminderList extends ArrayAdapter<Reminder> {

    private Context context;

    private String TAG = "ReminderList";

    public ArrayList<Reminder> values;

    /** On Click listener for expandable buttons **/
    public View.OnClickListener onClickListener;

    /** fragment type using this array adapter **/
    public int fragmentType = MainFragment.LIST_PENDING;

    public ReminderList(Context context, int textViewResourceId, ArrayList<Reminder> values, View.OnClickListener onClickListener,
                        int fragmentType){
        super(context, textViewResourceId, values);

        this.context = context;
        this.values = values;
        this.onClickListener = onClickListener;
        this.fragmentType = fragmentType;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){


        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.reminder_entry, parent, false);
        }

        TextView reminderBody = (TextView)convertView.findViewById(R.id.reminderDetailText);
        TextView reminderDateTime = (TextView)convertView.findViewById(R.id.reminderTimeText);
        // expanded view
        TextView reminderEdit = (TextView)convertView.findViewById(R.id.reminderEntryEdit);
        TextView reminderShare = (TextView)convertView.findViewById(R.id.reminderEntryShare);
        TextView reminderDismiss = (TextView)convertView.findViewById(R.id.reminderEntryDismiss);
        // set expanded section to GONE
        LinearLayout expandedView = (LinearLayout)convertView.findViewById(R.id.reminderExpanded);
        expandedView.setVisibility(View.GONE);

        // Set text description
        reminderBody.setText(values.get(position).getDescription());

        // Set reminder date and time
        reminderDateTime.setText(values.get(position).getDateTimeString(context));

        // set the on click listener
        reminderEdit.setOnClickListener(onClickListener);
        reminderShare.setOnClickListener(onClickListener);
        reminderDismiss.setOnClickListener(onClickListener);

        if(fragmentType == MainFragment.LIST_PENDING){
            reminderDismiss.setText(R.string.reminder_entry_dismiss);
        } else { // delete if already completed
            reminderDismiss.setText(R.string.reminder_entry_delete);
        }

        return convertView;
    }
}
