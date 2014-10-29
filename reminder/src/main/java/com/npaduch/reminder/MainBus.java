package com.npaduch.reminder;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Created by nolanpaduch on 10/28/14.
 *
 * Class to extend bus to allow communication from
 * service to main thread
 */
public class MainBus extends Bus {
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    MainBus.super.post(event);
                }
            });
        }
    }
}
