package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class S3PathContainerServiceTest {

    @Test
    public void testContainerVirtualHostInHostname() {
        assertFalse("/", new S3PathContainerService(new Host(new S3Protocol(), "b.s3.amazonaws.com")).isContainer(new Path("/b", EnumSet.of(Path.Type.directory))));
        assertFalse("/", new S3PathContainerService(new Host(new S3Protocol(), "b.s3.amazonaws.com")).isContainer(new Path("/b/f", EnumSet.of(Path.Type.file))));
        assertEquals("/", new S3PathContainerService(new Host(new S3Protocol(), "b.s3.amazonaws.com")).getContainer(new Path("/b/f", EnumSet.of(Path.Type.file))).getName());
    }

    @Test
    public void testContainer() {
        assertTrue("/", new S3PathContainerService(new Host(new S3Protocol(), "s3.amazonaws.com")).isContainer(new Path("/b", EnumSet.of(Path.Type.directory))));
        assertEquals("b", new S3PathContainerService(new Host(new S3Protocol(), "s3.amazonaws.com")).getContainer(new Path("/b/f", EnumSet.of(Path.Type.file))).getName());
        assertFalse("/", new S3PathContainerService(new Host(new S3Protocol(), "s3.amazonaws.com")).isContainer(new Path("/b/f", EnumSet.of(Path.Type.file))));
        assertEquals("b", new S3PathContainerService(new Host(new S3Protocol(), "s3.amazonaws.com")).getContainer(new Path("/b/f", EnumSet.of(Path.Type.file))).getName());
    }
}