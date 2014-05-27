package com.npaduch.reminder;

import android.content.Context;
import android.util.Log;
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
        if(MainActivity.reminders.get(position).getDescription().equals(Reminder.STRING_INIT)) {
            reminderBody.setText((R.string.reminder_example));
        }
        else {
            reminderBody.setText(MainActivity.reminders.get(position).getDescription());
        }
        if(values.get(position).getColor() == 0){ // no color has been set
            values.get(position).setColor(getColor());
        }

        //rowView.setBackgroundResource(getBackground());

        return rowView;
    }

    private int getBackground(){
        int bg = currentBackground++ % 5;
        switch(bg) {
            case BG_RED:
                return R.drawable.shadow_red;
            case BG_BLUE:
                return R.drawable.shadow_blue;
            case BG_GREEN:
                return R.drawable.shadow_green;
            case BG_PURPLE:
                return R.drawable.shadow_purple;
            case BG_ORANGE:
                return R.drawable.shadow_orange;
        }
        return R.drawable.shadow_red;
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
