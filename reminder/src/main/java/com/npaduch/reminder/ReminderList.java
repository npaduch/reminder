package com.npaduch.reminder;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nolanpaduch on 5/12/14.
 */
public class ReminderList extends ArrayAdapter<Reminder> {

    private Context context;

    ArrayList<Integer> colors;
    public Reminder values[];
    int currentColor = 0;

    public ReminderList(Context context, int textViewResourceId, Reminder values[]){
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
        TextView noteBody = (TextView)rowView.findViewById(R.id.reminderDetailText);
        noteBody.setText("temp_string");
        if(values[position].getColor() == 0){ // no color has been set
            values[position].setColor(getColor());
        }
        rowView.setBackgroundResource(values[position].getColor());

        return rowView;
    }

    private int getColor(){
        //int min = 0;
        //int max = 4;
        //int offset = min + (int)(Math.random() * ((max - min) + 1));
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
