package ch.cyberduck.core.worker;

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
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.ftp.FTPSession;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @version $Id$
 */
public class WritePermissionWorkerTest extends AbstractTestCase {

    @Test
    public void testRun() throws Exception {
        final Permission permission = new Permission(744);
        final Path path = new Path("a", EnumSet.of(Path.Type.directory));
        final WritePermissionWorker worker = new WritePermissionWorker(new FTPSession(new Host("h")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(new Path("b", EnumSet.of(Path.Type.file)));
                return children;
            }
        }, new UnixPermission() {
            @Override
            public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setUnixGroup(final Path file, final String group) throws BackgroundException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
                if(file.getName().equals("a")) {
                    assertEquals(new Permission(744), permission);
                }
                else if(file.getName().equals("b")) {
                    assertEquals(new Permission(644), permission);
                }
                else {
                    fail();
                }
            }
        }, Collections.singletonList(path), permission, true, new DisabledProgressListener()
        );
        worker.run();
    }

    @Test
    public void testRunRecursiveRetainDirectoryExecute() throws Exception {
        final Permission permission = new Permission(644);
        final Path a = new Path("a", EnumSet.of(Path.Type.directory));
        final WritePermissionWorker worker = new WritePermissionWorker(new FTPSession(new Host("h")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(a)) {
                    final AttributedList<Path> children = new AttributedList<Path>();
                    final Path d = new Path("d", EnumSet.of(Path.Type.directory));
                    d.attributes().setPermission(new Permission(744));
                    children.add(d);
                    children.add(new Path("f", EnumSet.of(Path.Type.file)));
                    return children;
                }
                return AttributedList.emptyList();
            }
        }, new UnixPermission() {
            @Override
            public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setUnixGroup(final Path file, final String group) throws BackgroundException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
                if(file.getName().equals("a")) {
                    assertEquals(new Permission(644), permission);
                }
                else if(file.getName().equals("d")) {
                    assertEquals(new Permission(544), permission);
                }
                else if(file.getName().equals("f")) {
                    assertEquals(new Permission(644), permission);
                }
                else {
                    fail();
                }
            }
        }, Collections.singletonList(a), permission, true, new DisabledProgressListener()
        );
        worker.run();
    }

    @Test
    public void testRetainStickyBit() throws Exception {
        final Permission permission = new Permission(744);
        final Path path = new Path("a", EnumSet.of(Path.Type.directory));
        path.attributes().setPermission(new Permission(Permission.Action.none, Permission.Action.none, Permission.Action.none,
                true, false, false));
        final WritePermissionWorker worker = new WritePermissionWorker(new FTPSession(new Host("h")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final AttributedList<Path> children = new AttributedList<Path>();
                children.add(new Path("b", EnumSet.of(Path.Type.file)));
                return children;
            }
        }, new UnixPermission() {
            @Override
            public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setUnixGroup(final Path file, final String group) throws BackgroundException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
                if(file.getName().equals("a")) {
                    assertEquals(new Permission(1744), permission);
                    assertEquals(permission, permission);
                }
                else if(file.getName().equals("b")) {
                    assertEquals(new Permission(644), permission);
                }
                else {
                    fail();
                }
            }
        }, Collections.singletonList(path), permission, true, new DisabledProgressListener()
        );
        worker.run();
    }
}
