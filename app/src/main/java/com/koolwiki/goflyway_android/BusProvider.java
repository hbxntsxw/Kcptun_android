package com.koolwiki.goflyway_android;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by shutup on 16/7/23.
 */
public class BusProvider {
    private static Bus ourInstance = new Bus(ThreadEnforcer.ANY);

    public static Bus getInstance() {
        return ourInstance;
    }

    private BusProvider() {
    }
}
