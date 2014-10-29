package com.npaduch.reminder;

import com.squareup.otto.Bus;

/**
 * Created by nolanpaduch on 7/10/14.
 *
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more efficient means
 * such as through injection directly into interested classes.
 */
final class BusProvider {
    private static final MainBus BUS = new MainBus();

    public static MainBus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}
