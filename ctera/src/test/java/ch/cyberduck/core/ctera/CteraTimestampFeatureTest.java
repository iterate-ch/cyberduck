package ch.cyberduck.core.ctera;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVMetadataFeature;
import ch.cyberduck.core.dav.DAVTimestampFeature;
import ch.cyberduck.core.dav.DAVTouchFeature;
import ch.cyberduck.core.dav.DAVWriteFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Category(IntegrationTest.class)
public class CteraTimestampFeatureTest extends AbstractCteraTest {

    @Test
    public void testSetTimestamp() throws Exception {
        final TransferStatus status = new TransferStatus();
        final Path file = new DAVTouchFeature(session).touch(new DAVWriteFeature(session), new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        new DAVTimestampFeature(session).setTimestamp(file, status.setModified(5100L));
        final PathAttributes attr = new CteraAttributesFinderFeature(session).find(file);
        assertEquals(5000L, attr.getModificationDate());
        assertEquals(status.getResponse(), attr);
        assertEquals(5000L, new DefaultAttributesFinderFeature(session).find(file).getModificationDate());
        new DAVMetadataFeature(session).setMetadata(file, status.setMetadata(Collections.singletonMap("Test", "Value")));
        assertEquals(5000L, new CteraAttributesFinderFeature(session).find(file).getModificationDate());
        new DAVMetadataFeature(session).setMetadata(file, status.setMetadata(Collections.emptyMap()));
        assertEquals(5000L, new CteraAttributesFinderFeature(session).find(file).getModificationDate());
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(file), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampFolderExplicitImplicit() throws Exception {
        final Path folder = new CteraDirectoryFeature(session).mkdir(new DAVWriteFeature(session), new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        new DAVTimestampFeature(session).setTimestamp(folder, 5100L);
        assertEquals(5000L, new CteraAttributesFinderFeature(session).find(folder).getModificationDate());
        assertEquals(5000L, new DefaultAttributesFinderFeature(session).find(folder).getModificationDate());
        Thread.sleep(1000L);
        final Path file = new DAVTouchFeature(session).touch(new DAVWriteFeature(session), new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotEquals(5000L, new CteraAttributesFinderFeature(session).find(folder).getModificationDate());
        assertNotEquals(5000L, new DefaultAttributesFinderFeature(session).find(folder).getModificationDate());
        new CteraDeleteFeature(session).delete(Arrays.asList(file, folder), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
