package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ftp.AbstractFTPTest;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class StatefulDefaultCopyFeatureTest extends AbstractFTPTest {

    @Test
    public void testSupported() throws Exception {
        final Path source = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertFalse(new DefaultCopyFeature(session).isSupported(source, target.getParent(), target.getName()));
        assertFalse(new DefaultCopyFeature(session).withTarget(session).isSupported(source, target.getParent(), target.getName()));
        assertTrue(new DefaultCopyFeature(session).withTarget(new FTPSession(new Host(session.getHost()).withCredentials(new Credentials("test", "test")))).isSupported(source, target.getParent(), target.getName()));
    }
}
