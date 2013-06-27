package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;

import org.junit.Test;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class RepeatableBackgroundActionTest extends AbstractTestCase {

    @Test
    public void testGetExceptions() throws Exception {
        final BackgroundException failure = new BackgroundException(null, null, null);
        RepeatableBackgroundAction a = new RepeatableBackgroundAction(new AlertCallback() {
            @Override
            public void alert(final RepeatableBackgroundAction repeatableBackgroundAction, final BackgroundException f, final StringBuilder transcript) {
                assertEquals(failure, f);
            }
        }, new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        }, new TranscriptListener() {
            @Override
            public void log(final boolean request, final String message) {
                //
            }
        }, new DisabledLoginController(), new DefaultHostKeyController()
        ) {

            @Override
            public List<Session<?>> getSessions() {
                return Collections.emptyList();
            }

            @Override
            public void run() throws BackgroundException {
                //
            }
        };
        a.error(new ConnectionCanceledException());
        assertFalse(a.hasFailed());
        assertNull(a.getException());
        a.error(failure);
        assertTrue(a.hasFailed());
        assertNotNull(a.getException());
    }

    @Test
    public void testRetrySocket() throws Exception {
        final BackgroundException failure = new BackgroundException(null, null, new SocketTimeoutException(""));
        RepeatableBackgroundAction a = new RepeatableBackgroundAction(new AlertCallback() {
            @Override
            public void alert(final RepeatableBackgroundAction repeatableBackgroundAction, final BackgroundException f, final StringBuilder transcript) {
                assertEquals(failure, f);
            }
        }, new ProgressListener() {
            @Override
            public void message(final String message) {
                //
            }
        }, new TranscriptListener() {
            @Override
            public void log(final boolean request, final String message) {
                //
            }
        }, new DisabledLoginController(), new DefaultHostKeyController()
        ) {
            @Override
            public List<Session<?>> getSessions() {
                return Collections.emptyList();
            }

            @Override
            public void run() throws BackgroundException {
                //
            }
        };
        a.error(failure);
        assertEquals(Preferences.instance().getInteger("connection.retry"), a.retry());
    }
}
