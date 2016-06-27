package ch.cyberduck.core;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.preferences.PreferencesFactory;

public final class IOKitSleepPreventer implements SleepPreventer {

    static {
        Native.load("core");
    }

    private static final String reason
            = PreferencesFactory.get().getProperty("application.name");

    protected IOKitSleepPreventer() {
        //
    }

    private static final Object lock = new Object();

    @Override
    public String lock() {
        synchronized(lock) {
            return this.createAssertion(reason);
        }
    }

    private native String createAssertion(String reason);

    @Override
    public void release(final String id) {
        synchronized(lock) {
            this.releaseAssertion(id);
        }
    }

    private native void releaseAssertion(String id);
}
