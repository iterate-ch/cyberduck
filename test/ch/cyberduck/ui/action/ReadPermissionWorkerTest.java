package ch.cyberduck.ui.action;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class ReadPermissionWorkerTest extends AbstractTestCase {


    @Test
    public void testRun() throws Exception {
        final ReadPermissionWorker worker = new ReadPermissionWorker(
                Arrays.<Path>asList(new Path("/a", Path.FILE_TYPE), new Path("/b", Path.FILE_TYPE))) {
            @Override
            public void cleanup(final List<Permission> result) {
                //
            }
        };
        assertEquals(2, worker.run().size());
        assertEquals("Getting permission of a… (2)", worker.getActivity());
    }
}
