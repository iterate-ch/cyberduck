package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class SMBTouchFeatureTest extends AbstractSMBTest {

    @Test
    public void testCaseSensitivity() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final String filename = StringUtils.lowerCase(new AlphanumericRandomStringService().random());
        final Path test = new SMBTouchFeature(session)
                .touch(new Path(home, filename, EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertThrows(ConflictException.class, () -> new SMBTouchFeature(session)
                .touch(new Path(home, StringUtils.upperCase(filename), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L)));
        assertTrue(new SMBFindFeature(session).find(test));
        assertTrue(new SMBFindFeature(session).find(new Path(home, StringUtils.upperCase(filename), EnumSet.of(Path.Type.file))));
        assertEquals(new SMBAttributesFinderFeature(session).find(test),
                new SMBAttributesFinderFeature(session).find(new Path(home, StringUtils.upperCase(filename), EnumSet.of(Path.Type.file))));
        new SMBDeleteFeature(session).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchLongFilename() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path test = new SMBTouchFeature(session).touch(
                new Path(home, new AlphanumericRandomStringService(130).random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new SMBDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
