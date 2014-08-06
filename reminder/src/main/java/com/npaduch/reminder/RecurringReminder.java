package com.npaduch.reminder;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import com.doomonafireball.betterpickers.recurrencepicker.EventRecurrence;
import com.doomonafireball.betterpickers.recurrencepicker.Utils;

import java.util.Calendar;

/**
 * Created by nolanpaduch on 7/28/14.
 *
 * This will handle all functions regarding recurring reminders
 */
public class RecurringReminder extends EventRecurrence {

    private final static String TAG = "RecurringReminder";

    public final static int INVALID_TIME = 0;
    public final static int FINAL_COUNT = -1;
    private final static int LAST_WEEK = -1;

    private final static int NOT_SPECIAL_CASE    = 0;
    private final static int OFFSET_IN_WEEK      = 1;

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
            if (this.freq == RecurringReminder.DAILY) {
                s += context.getResources().getString(R.string.repeat_day);
            } else if (this.freq == RecurringReminder.WEEKLY) {
                s += context.getResources().getString(R.string.repeat_week);
            } else if (this.freq == RecurringReminder.MONTHLY) {
                s += context.getResources().getString(R.string.repeat_month);
            } else if (this.freq == RecurringReminder.YEARLY) {
                s += context.getResources().getString(R.string.repeat_year);
            }
        } else {
            // add interval
            // Repeat every X days/weeks/months/years
            s += this.interval;
            s += " ";
            if (this.freq == RecurringReminder.DAILY) {
                s += context.getResources().getString(R.string.repeat_days);
            } else if (this.freq == RecurringReminder.WEEKLY) {
                s += context.getResources().getString(R.string.repeat_weeks);
            } else if (this.freq == RecurringReminder.MONTHLY) {
                s += context.getResources().getString(R.string.repeat_months);
            } else if (this.freq == RecurringReminder.YEARLY) {
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
                s += "1";
                s += " ";
                if(this.freq == RecurringReminder.DAILY){
                    s += context.getResources().getString(R.string.repeat_day);
                } else if (this.freq == RecurringReminder.WEEKLY){
                    s += context.getResources().getString(R.string.repeat_week);
                } else if (this.freq == RecurringReminder.MONTHLY){
                    s += context.getResources().getString(R.string.repeat_month);
                } else if (this.freq == RecurringReminder.YEARLY){
                    s += context.getResources().getString(R.string.repeat_year);
                }
            } else {
                s += this.count;
                s += " ";
                if(this.freq == RecurringReminder.DAILY){
                    s += context.getResources().getString(R.string.repeat_days);
                } else if (this.freq == RecurringReminder.WEEKLY){
                    s += context.getResources().getString(R.string.repeat_weeks);
                } else if (this.freq == RecurringReminder.MONTHLY){
                    s += context.getResources().getString(R.string.repeat_months);
                } else if (this.freq == RecurringReminder.YEARLY){
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
                    Log.d(TAG, "bydayNum "+i+" "+this.bydayNum[i]);
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
                if(this.bydayNum[0] == LAST_WEEK){
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

    private void dumpRecurrence(){
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



    private static final String[] suffixes =
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

    public long getNextAlertTime(Reminder r){

        int specialCase = NOT_SPECIAL_CASE;
        long previousTime = r.getMsTime();

        // set the calendar to the previous time, so we can add time
        Calendar nextReminderCal = Calendar.getInstance();
        nextReminderCal.setTimeInMillis(previousTime);

        // store previous calendar for comparison
        Calendar prevReminder = Calendar.getInstance();
        prevReminder.setTimeInMillis(previousTime);

        // Get the current time
        Calendar now = Calendar.getInstance();

        // make sure this is a valid call
        if(!isEnabled())
            return INVALID_TIME;


        // Check terminating conditions
        // First check if we're doing this until a certain date
        if (!TextUtils.isEmpty(this.until) && !this.until.equals("null")) {
            Calendar cal = Calendar.getInstance();
            Time t = new Time();
            t.parse(this.until);
            cal.setTimeInMillis(t.toMillis(false));

            if(now.after(cal)){
                Log.d(TAG, "Date is after terminating date.");
                return INVALID_TIME;
            }
        } else if (this.count != 0){
            // second check if we've exceeded the specified amount
            if(this.count == FINAL_COUNT){
                // This guy is done
                return INVALID_TIME;
            } else if (count == 1){
                // this is the last time we'll do this
                this.count = FINAL_COUNT;
            } else {
                // decrement count
                this.count--;
            }
        }

        // Check if we are in either of these cases:
        // 1. "Last" day of the month
        // 2. Multiple days in a week
        for(int i = 0; i < this.bydayCount; i++){
            if(this.bydayNum[i] != 0){
                // Case 1
                specialCase = OFFSET_IN_WEEK;
                break;
            }
        }
        if(specialCase == OFFSET_IN_WEEK){
            Log.d(TAG, "Special case: Offset in week");
            // get interval
            int newInterval = this.interval;
            if(newInterval == 0)
                newInterval++;
            // get the next month's offset
            if(this.bydayNum[0] == LAST_WEEK){
                // increment by 1 week until the month changes (interval+1)
                // then back off by one week
                while(nextReminderCal.get(Calendar.MONTH) != prevReminder.get(Calendar.MONTH)+newInterval+1){
                    Log.d(TAG,"We're on the last week.");
                    nextReminderCal.add(Calendar.WEEK_OF_YEAR, 1);
                }
                nextReminderCal.add(Calendar.WEEK_OF_YEAR, -1);
            } else {
                // specific offset in week
                // increment until we hit next month
                // then increment by offset in week
                while(nextReminderCal.get(Calendar.MONTH) != prevReminder.get(Calendar.MONTH)+newInterval) {
                    nextReminderCal.add(Calendar.WEEK_OF_YEAR, 1);
                }
                // we're already on the first week, so bump by week offset -1
                nextReminderCal.add(Calendar.WEEK_OF_YEAR, this.bydayNum[0]-1);
            }

            // All done!
            return nextReminderCal.getTimeInMillis();
        }
        // Check for case 2
        if(this.bydayCount > 1){
            // find what day we are currently on
            int currentDay = now.get(Calendar.DAY_OF_WEEK);
            // build map array. Position 0 is reserved since days are 1-based
            int days[] = {0,0,0,0,0,0,0,0};
            for(int i = 0; i < this.bydayCount; i++) {
                days[day2CalendarDay(this.byday[i])] = 1;
            }
            // in case today is one of the days, increment current day to
            currentDay++;
            if(currentDay == days.length)
                currentDay = 1;
            while(days[currentDay] != 1){
                currentDay++;
                if(currentDay == days.length)
                    currentDay = 1;
            }

            // at this point, current day should be equal to the next reminder day
            int nextDay = currentDay;

            // now we have the day. Increment by at least 1 day until we hit the next one
            nextReminderCal.add(Calendar.DAY_OF_WEEK, 1);
            while(nextReminderCal.get(Calendar.DAY_OF_WEEK)
                    != nextDay){
                // Check if we need to increment # weeks
                nextReminderCal.add(Calendar.DAY_OF_WEEK, 1);
                if(nextReminderCal.getFirstDayOfWeek() == nextReminderCal.get(Calendar.DAY_OF_WEEK)){
                    int newInterval = this.interval;
                    if(newInterval != 0){
                        // subtract one week, since we're already cycling around the week
                        newInterval--;
                    }
                    nextReminderCal.add(Calendar.WEEK_OF_YEAR, newInterval);
                }
            }

            // All done!
            return nextReminderCal.getTimeInMillis();
        }

        // easy mode
        // just increment by the interval to get the next time

        // an interval of 0 is actually one....
        int amountToAdd = this.interval;
        if(this.interval == 0)
            amountToAdd++;

        if(this.freq == RecurringReminder.DAILY){
            nextReminderCal.add(Calendar.DAY_OF_YEAR, amountToAdd);
        } else if (this.freq == RecurringReminder.WEEKLY){
            nextReminderCal.add(Calendar.WEEK_OF_YEAR, amountToAdd);
        } else if (this.freq == RecurringReminder.MONTHLY){
            nextReminderCal.add(Calendar.MONTH, amountToAdd);
        } else if (this.freq == RecurringReminder.YEARLY){
            nextReminderCal.add(Calendar.YEAR, amountToAdd);
        }

        return nextReminderCal.getTimeInMillis();
    }

    /**
     * TESTING:
     *
     * every day forever                    - DONE
     * every day for count                  - DONE
     * every day until                      - DONE
     * --> Count + Until are now complete   - DONE
     * every X days forever                 - DONE
     * --> Daily is now complete            - DONE
     *
     * every week forever just one day      - DONE
     * every week multiple days             - DONE
     * every X weeks multiple days          - DONE
     *
     * every month forever                  - DONE
     * every month same day                 - DONE (same as above)
     * every month week offset              - DONE (including last)
     * every X months same day              - DONE
     * every X months week offset           - DONE
     *
     * every year forever                   - DONE
     * every X years                        - DONE
     */
}