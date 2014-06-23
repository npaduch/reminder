package com.npaduch.reminder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by nolanpaduch on 5/12/14.
 */
public class ReminderList extends ArrayAdapter<Reminder> {

    private Context context;

    private String TAG = "ReminderList";

    public ArrayList<Reminder> values;

    public ReminderList(Context context, int textViewResourceId, ArrayList<Reminder> values){
        super(context, textViewResourceId, values);

        this.context = context;
        this.values = values;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.reminder_entry, parent, false);
        TextView reminderBody = (TextView)rowView.findViewById(R.id.reminderDetailText);

        // Set text description
        if(values.get(position).getDescription().equals(Reminder.STRING_INIT)) {
            reminderBody.setText((R.string.reminder_example));
        }
        else {
            reminderBody.setText(MainActivity.reminders.get(position).getDescription());
        }

        // Set reminder date
        TextView reminderDateTime = (TextView) rowView.findViewById(R.id.reminderTimeText);
        if(values.get(position).getDateTimeString().equals(Reminder.STRING_INIT)) {
            reminderDateTime.setText(R.string.reminder_time_beginning);
        }
        else {
            reminderDateTime.setText(
                    MainActivity.reminders.get(position).getDateTimeString()
            );
        }


        return rowView;
    }
}
