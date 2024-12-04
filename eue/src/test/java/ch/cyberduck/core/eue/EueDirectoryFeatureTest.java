package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@Category(IntegrationTest.class)
public class EueDirectoryFeatureTest extends AbstractEueSessionTest {

    @Test
    public void testAttributes() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final TransferStatus status = new TransferStatus();
        final Path directory = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), status);
        assertThrows(ConflictException.class, () -> new EueDirectoryFeature(session, fileid).mkdir(directory, new TransferStatus()));
        assertEquals(new EueAttributesFinderFeature(session, fileid).find(directory).getFileId(), directory.attributes().getFileId());
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testProhibitedName() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        try {
            new EueDirectoryFeature(session, fileid).mkdir(new Path(String.format("%s.", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory)), new TransferStatus());
        }
        catch(InteroperabilityException e) {
            assertEquals("Paths may not end with a . Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testCaseSensitivity() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final String filename = new AlphanumericRandomStringService().random();
        new EueDirectoryFeature(session, fileid).mkdir(new Path(StringUtils.capitalize(filename), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertThrows(ConflictException.class, () -> new EueDirectoryFeature(session, fileid).mkdir(new Path(StringUtils.lowerCase(filename), EnumSet.of(Path.Type.directory)), new TransferStatus()));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(new Path(StringUtils.capitalize(filename), EnumSet.of(Path.Type.directory))), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}