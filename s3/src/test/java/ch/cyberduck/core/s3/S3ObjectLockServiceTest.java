package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class S3ObjectLockServiceTest extends AbstractS3Test {

    @Test
    public void testDisabled() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertFalse(new S3ObjectLockService(session).isObjectLockEnabled(container));
        assertFalse(new S3ObjectLockService(session).isObjectLockEnabled(new Path(container, "f", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testEnabled() throws Exception {
        final Path container = new Path("test-eu-central-1-objectlock", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3ObjectLockService(session).isObjectLockEnabled(container));
        assertTrue(new S3ObjectLockService(session).isObjectLockEnabled(new Path(container, "f", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testRoot() throws Exception {
        assertFalse(new S3ObjectLockService(session).isObjectLockEnabled(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }
}
