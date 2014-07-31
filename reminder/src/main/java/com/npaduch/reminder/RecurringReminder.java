package com.npaduch.reminder;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrence;

import java.util.Calendar;

/**
 * Created by nolanpaduch on 7/28/14.
 *
 * This will handle all functions regarding recurring reminders
 */
public class RecurringReminder extends EventRecurrence {

    private final static String TAG = "RecurringReminder";

    private boolean enabled;

    public RecurringReminder (){
        super();
        enabled = false;
    }

    public String makeString(Context context){

        // Repeat every
        String s = context.getResources().getString(R.string.new_reminder_recurrence_base);

        s += " ";

        // add interval
        if(this.interval == 0) {
            // repeat every
            // add frequency
            // Repeat every day/week/month/year
            if (this.freq == this.DAILY) {
                s += context.getResources().getString(R.string.repeat_day);
            } else if (this.freq == this.WEEKLY) {
                s += context.getResources().getString(R.string.repeat_week);
            } else if (this.freq == this.MONTHLY) {
                s += context.getResources().getString(R.string.repeat_month);
            } else if (this.freq == this.YEARLY) {
                s += context.getResources().getString(R.string.repeat_year);
            }
        } else {
            // add interval
            // Repeat every X days/weeks/months/years
            s += this.interval;
            s += " ";
            if (this.freq == this.DAILY) {
                s += context.getResources().getString(R.string.repeat_days);
            } else if (this.freq == this.WEEKLY) {
                s += context.getResources().getString(R.string.repeat_weeks);
            } else if (this.freq == this.MONTHLY) {
                s += context.getResources().getString(R.string.repeat_months);
            } else if (this.freq == this.YEARLY) {
                s += context.getResources().getString(R.string.repeat_years);
            }
        }

        s += " ";

        // until or count
        // Repeat every day/week/month/year until January 1, 2001
        if (!TextUtils.isEmpty(this.until) && !this.until.equals("null")) {
            s += context.getResources().getString(R.string.time_until);
            s += " ";
            Calendar cal = Calendar.getInstance();
            Time t = new Time();
            t.parse(this.until);
            cal.setTimeInMillis(t.toMillis(false));
            s += Reminder.buildDateString(context, cal);
            s += " ";
        }

        // Repeat every day/week/month/year for X day(s)/week(s)/month(s)/year(s)
        if (this.count != 0) {
            s += context.getResources().getString(R.string.time_for);
            s += " ";
            if(count == 1){
                if(this.freq == this.DAILY){
                    s += context.getResources().getString(R.string.repeat_day);
                } else if (this.freq == this.WEEKLY){
                    s += context.getResources().getString(R.string.repeat_week);
                } else if (this.freq == this.MONTHLY){
                    s += context.getResources().getString(R.string.repeat_month);
                } else if (this.freq == this.YEARLY){
                    s += context.getResources().getString(R.string.repeat_year);
                }
            } else {
                s += this.count;
                s += " ";
                if(this.freq == this.DAILY){
                    s += context.getResources().getString(R.string.repeat_days);
                } else if (this.freq == this.WEEKLY){
                    s += context.getResources().getString(R.string.repeat_weeks);
                } else if (this.freq == this.MONTHLY){
                    s += context.getResources().getString(R.string.repeat_months);
                } else if (this.freq == this.YEARLY){
                    s += context.getResources().getString(R.string.repeat_years);
                }
            }
            s += " ";
        }


        // Add in days
        // Repeat every day/week/month/year for X day(s)/week(s)/month(s)/year(s) on Monday
        // day
        if (this.bydayCount > 0) {
            s += context.getResources().getString(R.string.time_on);

            // Check if this is a "4th monday of the month" situation
            boolean specialCase = false;
            for(int i = 0; i < this.bydayCount; i++){
                if(this.bydayNum[i] != 0){
                    Log.d(TAG, "bydaynum "+i+" "+this.bydayNum[i]);
                    specialCase = true;
                }
            }

            s += " ";

            if(!specialCase) {
                // day1, day2, and day3 || day1 and day2 || day1
                for (int i = 0; i < this.bydayCount; i++) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_WEEK, day2CalendarDay(this.byday[i]));
                    s += cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, context.getResources().getConfiguration().locale);
                    // add comma
                    if (i <= this.bydayCount - 2 && this.bydayCount > 2) {
                        s += ",";
                        s += " ";
                    } else if (i == this.bydayCount - 2 && this.bydayCount == 2){
                        s += " ";
                    }
                    if (i == this.bydayCount - 2) {
                        s += context.getResources().getString(R.string.time_and);
                        s += " ";
                    }
                }
            } else {
                // special case of "the 4th monday of the month"
                s += context.getResources().getString(R.string.time_the);
                s += " ";
                if(this.bydayNum[0] == -1){
                    s += context.getResources().getString(R.string.time_last);
                } else {
                    s += this.bydayNum[0]+suffixes[this.bydayNum[0]];
                }
                s += " ";
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_WEEK, day2CalendarDay(this.byday[0]));
                s += cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, context.getResources().getConfiguration().locale);
                s += " ";
                s += context.getResources().getString(R.string.time_of_the_month);
            }
        }

        dumpRecurrence();
        String test = encode();
        Log.d(TAG, "Encoded string: "+test);
        decode(test);
        dumpRecurrence();

        return s;
    }

    public String encode(){
        // enabled, interval, until, count, freq, byDayCount, byDayNum[]
        String s = "";
        String separator = "<>";
        s += this.enabled + separator;
        s += this.interval + separator;
        s += this.until + separator;
        s += this.count + separator;
        s += this.freq + separator;
        s += this.bydayCount + separator;
        for(int i = 0; i < this.bydayCount; i++)
            s += this.bydayNum[i] + separator;
        for(int i = 0; i < this.bydayCount; i++)
            s += this.byday[i] + separator;

        return s;
    }

    public void decode(String s){
        // interval, until, count, freq, byDayCount, byDayNum[], byDay
        int offset = 0;
        String sub[] = s.split("<>");
        this.enabled = Boolean.parseBoolean(sub[offset++]);
        this.interval = Integer.parseInt(sub[offset++]);
        this.until = sub[offset++];
        this.count = Integer.parseInt(sub[offset++]);
        this.freq = Integer.parseInt(sub[offset++]);
        this.bydayCount = Integer.parseInt(sub[offset++]);
        this.bydayNum  = new int[this.bydayCount];
        for(int i = 0; i < this.bydayCount; i++)
            this.bydayNum[i] = Integer.parseInt(sub[offset++]);
        this.byday = new int[this.bydayCount];
        for(int i = 0; i < this.bydayCount; i++)
            this.byday[i] = Integer.parseInt(sub[offset++]);
    }

    public void dumpRecurrence(){
        Log.d(TAG, "RECURRENCE START");
        Log.d(TAG, "Enabled: "+this.enabled);                   // recurrence enabled
        Log.d(TAG, "Interval: "+this.interval);                 // interval (i.e. every 3 days)
        Log.d(TAG, "Until: "+this.until);                       // Date to be reminded until
        Log.d(TAG, "Count: "+this.count);                       // Number of times to recur
        Log.d(TAG, "Freq: "+this.freq);                         // How frequent (Day/Week/Month/Year)
        Log.d(TAG, "BydayCount: "+this.bydayCount);             // total days to be reminded on during the week
        for(int i = 0; i < this.bydayCount; i++)
            Log.d(TAG, "BydayNum["+i+"]: "+this.bydayNum[i]);   // Week offset for day to be reminded on (i.e. 3rd Wednesday)
        for(int i = 0; i < this.bydayCount; i++)
            Log.d(TAG, "Byday["+i+"]: "+this.byday[i]);         // Day, i.e. Monday
        Log.d(TAG, "RECURRENCE END");
    }



    static String[] suffixes =
            //    0     1     2     3     4     5     6     7     8     9
            { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
            //    10    11    12    13    14    15    16    17    18    19
            "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
            //    20    21    22    23    24    25    26    27    28    29
            "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
            //    30    31
            "th", "st"
            };


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
