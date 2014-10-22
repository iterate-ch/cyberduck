package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import org.apache.commons.lang3.concurrent.TimedSemaphore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertTrue;

public class LimitedRendezvousListenerTest extends AbstractTestCase {

    @Test
    public void testServiceResolved() throws Exception {
        final AtomicBoolean notified = new AtomicBoolean();
        final AtomicLong ts = new AtomicLong(System.currentTimeMillis());
        // Test with limit of one notification per second
        final LimitedRendezvousListener l = new LimitedRendezvousListener(new TimedSemaphore(
                1L, TimeUnit.SECONDS, 1),
                new HashSet<RendezvousListener>(Collections.singletonList(new RendezvousListener() {
                    @Override
                    public void serviceResolved(final String identifier, final Host host) {
                        final long now = System.currentTimeMillis();
                        assertTrue(TimeUnit.SECONDS.toMillis(1) > (now - ts.get()));
                        notified.set(true);
                    }

                    @Override
                    public void serviceLost(final Host host) {
                        //
                    }
                })));
        long start = System.currentTimeMillis();
        for(int i = 0; i < 100; i++) {
            l.serviceResolved("i", new Host("l"));
        }
        assertTrue(notified.get());
    }
}