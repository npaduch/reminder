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

    private final int BG_RED      = 0;
    private final int BG_BLUE     = 1;
    private final int BG_GREEN    = 2;
    private final int BG_PURPLE   = 3;
    private final int BG_ORANGE   = 4;
    private int currentBackground = 0;

    ArrayList<Integer> colors;
    public ArrayList<Reminder> values;
    int currentColor = 0;

    public ReminderList(Context context, int textViewResourceId, ArrayList<Reminder> values){
        super(context, textViewResourceId, values);

        this.context = context;
        this.values = values;

        // Array of colors
        colors = new ArrayList<Integer>();
        populateColorList();

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.reminder_entry, parent, false);
        TextView reminderBody = (TextView)rowView.findViewById(R.id.reminderDetailText);

        // Set text description
        if(MainActivity.reminders.get(position).getDescription().equals(Reminder.STRING_INIT)) {
            reminderBody.setText((R.string.reminder_example));
        }
        else {
            reminderBody.setText(MainActivity.reminders.get(position).getDescription());
        }

        // Set reminder date
        TextView reminderDateTime = (TextView) rowView.findViewById(R.id.reminderTimeText);
        if(MainActivity.reminders.get(position).getDateTimeString().equals(Reminder.STRING_INIT)) {
            reminderDateTime.setText(R.string.reminder_time_beginning);
        }
        else {
            reminderDateTime.setText(
                    MainActivity.reminders.get(position).getDateTimeString()
            );
        }


        return rowView;
    }

    private int getColor(){
        return colors.get(currentColor++ % 5);
    }

    private void populateColorList(){
        colors.add(R.color.blue);
        colors.add(R.color.purple);
        colors.add(R.color.green);
        colors.add(R.color.orange);
        colors.add(R.color.red);
    }

}
