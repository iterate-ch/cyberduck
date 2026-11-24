package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.preferences.MemoryPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LocalFactoryTest {

    /**
     * Setting preferences must not deadlock with another thread creating a {@link Local}. Creating a {@link Local} reads
     * the preferences, and setting preferences creates a {@link Local} to load the default properties.
     */
    @Test(timeout = 30000)
    public void testGetWhileSettingPreferences() throws Exception {
        final AtomicReference<Local> created = new AtomicReference<>();
        final Thread[] other = new Thread[1];
        PreferencesFactory.set(new MemoryPreferences() {
            @Override
            protected void setDefaults() {
                super.setDefaults();
                // Called with the lock of the preferences factory held
                other[0] = new Thread(() -> created.set(LocalFactory.get("/tmp/test")), "local-factory-test");
                other[0].setDaemon(true);
                other[0].start();
                // Wait until the thread is blocked reading preferences, holding whatever lock it acquired before
                final long timeout = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                while(other[0].getState() != Thread.State.BLOCKED && System.nanoTime() < timeout) {
                    Thread.yield();
                }
                assertEquals(Thread.State.BLOCKED, other[0].getState());
            }
        });
        // Completes only if setting preferences did not wait for a lock held by the blocked thread
        other[0].join(TimeUnit.SECONDS.toMillis(10));
        assertTrue(!other[0].isAlive());
        assertNotNull(created.get());
        assertEquals("/tmp/test", created.get().getAbsolute());
    }
}
