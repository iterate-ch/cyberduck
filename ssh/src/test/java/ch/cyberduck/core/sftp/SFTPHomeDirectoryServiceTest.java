package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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

import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class SFTPHomeDirectoryServiceTest extends AbstractSFTPTest {

    @Test
    public void testFind() throws Exception {
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory)), new SFTPHomeDirectoryService(session).find());

    }

    @Test
    public void testFindWithWorkdir() throws Exception {
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
            new SFTPHomeDirectoryService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "sandbox"));
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
            new SFTPHomeDirectoryService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "/sandbox"));
    }
}
