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
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.test.NullSession;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;

/**
 * @version $Id$
 */
public class WritePermissionWorkerTest extends AbstractTestCase {

    @Test
    public void testRun() throws Exception {
        final Permission permission = new Permission(744);
        final Path path = new Path("a", EnumSet.of(Path.Type.directory));
        final WritePermissionWorker worker = new WritePermissionWorker(Collections.singletonList(path), permission, true, new DisabledProgressListener()
        );
        worker.run(new NullSession(new Host("")));
    }

    @Test
    public void testRunRecursiveRetainDirectoryExecute() throws Exception {
        final Permission permission = new Permission(644);
        final Path a = new Path("a", EnumSet.of(Path.Type.directory));
        final WritePermissionWorker worker = new WritePermissionWorker(Collections.singletonList(a), permission, true, new DisabledProgressListener()
        );
        worker.run(new NullSession(new Host("")));
    }

    @Test
    public void testRunRecursiveSetDirectoryExecute() throws Exception {
        final Path a = new Path("a", EnumSet.of(Path.Type.directory));
        a.attributes().setPermission(new Permission(774));
        final Path f = new Path("f", EnumSet.of(Path.Type.file));
        final Path d = new Path("d", EnumSet.of(Path.Type.directory));
        final WritePermissionWorker worker = new WritePermissionWorker(Collections.singletonList(a), new Permission(775), true, new DisabledProgressListener()
        );
        worker.run(new NullSession(new Host("")));
    }

    @Test
    public void testRetainStickyBit() throws Exception {
        final Permission permission = new Permission(744);
        final Path path = new Path("a", EnumSet.of(Path.Type.directory));
        path.attributes().setPermission(new Permission(Permission.Action.none, Permission.Action.none, Permission.Action.none,
                true, false, false));
        final WritePermissionWorker worker = new WritePermissionWorker(Collections.singletonList(path), permission, true, new DisabledProgressListener()
        );
        worker.run(new NullSession(new Host("")));
    }
}
