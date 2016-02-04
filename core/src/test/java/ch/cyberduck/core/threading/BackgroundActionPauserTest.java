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

import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;

import org.junit.Test;

import static org.junit.Assert.*;

public class BackgroundActionPauserTest {

    @Test
    public void testAwait() throws Exception {
        final AbstractBackgroundAction action = new AbstractBackgroundAction() {
            @Override
            public Object run() throws BackgroundException {
                throw new BackgroundException(new RuntimeException("f"));
            }
        };
        try {
            action.call();
            fail();
        }
        catch(BackgroundException e) {
            //
        }
        new BackgroundActionPauser(new BackgroundActionPauser.Callback() {
            @Override
            public boolean isCanceled() {
                return action.isCanceled();
            }

            @Override
            public void progress(final Integer delay) {
                //
            }
        }).await(new ProgressListener() {
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
