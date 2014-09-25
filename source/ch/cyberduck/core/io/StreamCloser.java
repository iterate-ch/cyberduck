package ch.cyberduck.core.io;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.threading.NamedThreadFactory;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @version $Id$
 */
public class StreamCloser {

    private final Preferences preferences
            = Preferences.instance();

    private final ThreadFactory f
            = new NamedThreadFactory("close");

    public void close(final InputStream in) {
        final CountDownLatch signal = new CountDownLatch(1);
        new NamedThreadFactory("close").newThread(new Runnable() {
            @Override
            public void run() {
                IOUtils.closeQuietly(in);
                signal.countDown();
            }
        }).start();
        try {
            signal.await(preferences.getInteger("connection.timeout.seconds"), TimeUnit.SECONDS);
        }
        catch(InterruptedException e) {
            //
        }
    }

    public void close(final OutputStream out) {
        final CountDownLatch signal = new CountDownLatch(1);
        new NamedThreadFactory("close").newThread(new Runnable() {
            @Override
            public void run() {
                IOUtils.closeQuietly(out);
                signal.countDown();
            }
        }).start();
        try {
            signal.await(preferences.getInteger("connection.timeout.seconds"), TimeUnit.SECONDS);
        }
        catch(InterruptedException e) {
            //
        }
    }
}
