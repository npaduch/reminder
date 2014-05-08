package com.npaduch.reminder;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by nolanpaduch on 5/7/14.
 *
 * Class is a holder for all the view involved
 * with a reminder card. The idea is to create 1
 * view instead of several from within the
 * main fragment.
 */
public class ReminderCardView {

    LinearLayout main;
    ImageView editButton;
    TextView description;
    TextView time;

    public ReminderCardView(Context context){
        main = new LinearLayout(context);
        main.setLayoutParams( new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        editButton = new ImageView(context);
        description = new TextView(context);
        time = new TextView(context);

    }
}
