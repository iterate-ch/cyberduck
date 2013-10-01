package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class BackgroundActionPauserTest extends AbstractTestCase {

    @Test
    public void testAwait() throws Exception {
        final SessionBackgroundAction action = new SessionBackgroundAction(Collections.emptyList(), Cache.empty(), new AlertCallback() {
            @Override
            public void alert(final SessionBackgroundAction action, final BackgroundException failure, final StringBuilder transcript) {
                //
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
            public Object run() throws BackgroundException {
                throw new BackgroundException(new RuntimeException("f"));
            }
        };
        action.call();
        new BackgroundActionPauser(action).await(new ProgressListener() {
            String previous;

            @Override
            public void message(final String message) {
                assertNotNull(message);
                assertNotEquals(previous, message);
                previous = message;
            }
        });
    }
}
