package ch.cyberduck.core.manta;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MantaHomeFeatureIT extends AbstractMantaTest {

    @Test
    public void testHomeFeature() throws BackgroundException {
        final Path drive = new MantaHomeFinderFeature(session).find();
        assertNotNull(drive);
        assertFalse(drive.isRoot());
        assertTrue(drive.isPlaceholder());
        assertNotEquals("null", drive.getName());
        assertFalse(StringUtils.isEmpty(drive.getName()));
    }

    @Test
    public void testPrivateRoot() throws BackgroundException {
        final Path drive = session.getAccountPrivateRoot();
        assertNotNull(drive);
        assertTrue(drive.isVolume());
        assertTrue(drive.isDirectory());
        assertEquals("stor", drive.getName());
        assertFalse(StringUtils.isEmpty(drive.getName()));
    }

    @Test
    public void testPublicRoot() throws BackgroundException {
        final Path drive = session.getAccountPublicRoot();
        assertNotNull(drive);
        assertTrue(drive.isVolume());
        assertTrue(drive.isDirectory());
        assertEquals("public", drive.getName());
        assertFalse(StringUtils.isEmpty(drive.getName()));
    }
}
