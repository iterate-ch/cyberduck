package ch.cyberduck.core.sftp;

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
import ch.cyberduck.core.TranscriptListener;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SFTPCommandFeatureTest extends AbstractSFTPTest {

    @Test
    @Ignore
    public void testSend() throws Exception {
        final StringBuilder t = new StringBuilder();
        new SFTPCommandFeature(session).send("ps", new ProgressListener() {
            @Override
            public void message(final String message) {
                assertEquals("ps", message);
            }
        }, new TranscriptListener() {
            @Override
            public void log(final Type request, final String message) {
                switch(request) {
                    case response:
                        t.append(message);
                }
            }
        });
        assertTrue("PID TTY          TIME CMD22417 ?        00:00:00 sshd22418 ?        00:00:00 sftp-server22427 ?        00:00:00 ps", t.toString().startsWith(
            "  PID TTY          TIME"));

    }
}
