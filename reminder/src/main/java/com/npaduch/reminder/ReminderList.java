package com.npaduch.reminder;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by nolanpaduch on 5/12/14.
 */
public class ReminderList extends ArrayAdapter<Reminder> {

    private Context context;

    private String TAG = "ReminderList";

    public ArrayList<Reminder> values;

    public View.OnClickListener onClickListener;

    public ReminderList(Context context, int textViewResourceId, ArrayList<Reminder> values, View.OnClickListener onClickListener){
        super(context, textViewResourceId, values);

        this.context = context;
        this.values = values;
        this.onClickListener = onClickListener;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.reminder_entry, parent, false);
        TextView reminderBody = (TextView)rowView.findViewById(R.id.reminderDetailText);
        TextView reminderDateTime = (TextView)rowView.findViewById(R.id.reminderTimeText);
        // expanded view
        TextView reminderEdit = (TextView)rowView.findViewById(R.id.reminderEntryEdit);
        TextView reminderShare = (TextView)rowView.findViewById(R.id.reminderEntryShare);
        TextView reminderDismiss = (TextView)rowView.findViewById(R.id.reminderEntryDismiss);

        // Set text description
        reminderBody.setText(values.get(position).getDescription());

        // Set reminder date and time
        reminderDateTime.setText(values.get(position).getDateTimeString(context));

        // set the on click listener
        reminderEdit.setOnClickListener(onClickListener);
        reminderShare.setOnClickListener(onClickListener);
        reminderDismiss.setOnClickListener(onClickListener);

        return rowView;
    }
}
