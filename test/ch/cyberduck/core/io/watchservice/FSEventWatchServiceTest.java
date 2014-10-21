package ch.cyberduck.core.io.watchservice;

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

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FSEventWatchServiceTest extends AbstractTestCase {

    @Test
    public void testRegister() throws Exception {
        final FSEventWatchService fs = new FSEventWatchService();
        final WatchableFile file = new WatchableFile(
                File.createTempFile(UUID.randomUUID().toString(), "t"));
        final WatchKey key = file.register(fs);
        while(!key.isValid()) {
            Thread.sleep(1000);
        }
        assertTrue(key.isValid());
        fs.close();
        assertFalse(key.isValid());
    }
}