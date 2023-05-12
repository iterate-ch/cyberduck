package ch.cyberduck.core.smb;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SMBDirectoryFeatureTest extends AbstractSMBTest {

    @Test
    public void testMakeDirectory() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        session.getFeature(Directory.class).mkdir(test, new TransferStatus());
        assertTrue(session.getFeature(Find.class).find(test));
        assertThrows(ConflictException.class, () -> session.getFeature(Directory.class).mkdir(test, new TransferStatus()));
    }
}
