/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
 */

package ch.cyberduck.core.local;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.io.watchservice.DisabledWatchService;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileWatcherTest {

    @Test
    public void testMatchDefaultLocal() throws IOException, LocalAccessDeniedException {
        assertTrue(
                new FileWatcher(new DisabledWatchService()).matches(
                        new Local("/private/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"),
                        new Local("/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"))
        );
        assertTrue(
                new FileWatcher(new DisabledWatchService()).matches(
                        new Local("/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"),
                        new Local("/private/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"))
        );
        assertFalse(
                new FileWatcher(new DisabledWatchService()).matches(
                        new Local("/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"),
                        new Local("/private/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/d/edit.html"))
        );
        assertFalse(
                new FileWatcher(new DisabledWatchService()).matches(
                        new Local("/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/d/edit.html"),
                        new Local("/private/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"))
        );
        assertTrue(
                new FileWatcher(new DisabledWatchService()).matches(
                        new Local("edit.html"),
                        new Local("/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"))
        );
        assertTrue(
                new FileWatcher(new DisabledWatchService()).matches(
                        new Local("edit.html"),
                        new Local("/private/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"))
        );
    }

    @Test
    public void testMatchFinderLocal() throws IOException, LocalAccessDeniedException {
        assertTrue(
                new FileWatcher(new DisabledWatchService()).matches(
                        new FinderLocal("/private/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"),
                        new FinderLocal("/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"))
        );
        assertTrue(
                new FileWatcher(new DisabledWatchService()).matches(
                        new FinderLocal("/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"),
                        new FinderLocal("/private/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"))
        );
        assertFalse(
                new FileWatcher(new DisabledWatchService()).matches(
                        new FinderLocal("/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"),
                        new FinderLocal("/private/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/d/edit.html"))
        );
        assertFalse(
                new FileWatcher(new DisabledWatchService()).matches(
                        new FinderLocal("/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/d/edit.html"),
                        new FinderLocal("/private/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"))
        );
        assertTrue(
                new FileWatcher(new DisabledWatchService()).matches(
                        new FinderLocal("edit.html"),
                        new FinderLocal("/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"))
        );
        assertTrue(
                new FileWatcher(new DisabledWatchService()).matches(
                        new FinderLocal("edit.html"),
                        new FinderLocal("/private/var/folders/cl/622z57616532npsw3xs1ndyc0000gp/T/1022b1a9-c21c-4a79-9162-59990c75aaa8/usr/home/dkocher/sandbox/edit.html"))
        );
    }
}