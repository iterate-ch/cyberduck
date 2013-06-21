package ch.cyberduck.ui.action;

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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class WritePermissionWorkerTest extends AbstractTestCase {

    @Test
    public void testRun() throws Exception {
        final Permission permission = new Permission(744);
        final NullPath path = new NullPath("a", Path.DIRECTORY_TYPE) {
            @Override
            public void writeUnixPermission(Permission p) {
                assertEquals(permission, p);
            }

            @Override
            public AttributedList<Path> list() {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(new NullPath("b", Path.FILE_TYPE) {
                    @Override
                    public void writeUnixPermission(Permission p) {
                        assertEquals(new Permission(644), p);
                    }
                });
                return children;
            }
        };
        final WritePermissionWorker worker = new WritePermissionWorker(Arrays.<Path>asList(path), permission, true) {
            @Override
            public void cleanup(Permission result) {
                throw new UnsupportedOperationException();
            }
        };
        assertEquals(new Permission(744), worker.run());
    }
}
