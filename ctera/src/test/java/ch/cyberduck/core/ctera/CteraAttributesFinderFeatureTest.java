package ch.cyberduck.core.ctera;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CteraAttributesFinderFeatureTest extends AbstractCteraTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DAVAttributesFinderFeature f = new CteraAttributesFinderFeature(session);
        f.find(test);
        fail();
    }

    @Test
    public void testFindFile() throws Exception {
        final Path root = new DefaultHomeFinderService(session).find();
        final DAVAttributesFinderFeature f = new CteraAttributesFinderFeature(session);
        final long rootTimestamp = f.find(root).getModificationDate();
        final String rootEtag = f.find(root).getETag();
        // No milliseconds precision
        Thread.sleep(1000L);
        final Path folder = new CteraDirectoryFeature(session).mkdir(new Path(root,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotEquals(rootTimestamp, f.find(root).getModificationDate());
        assertNotEquals(rootEtag, f.find(root).getETag());
        final long folderTimestamp = f.find(folder).getModificationDate();
        final String folderEtag = f.find(folder).getETag();
        final Path test = new CteraTouchFeature(session).touch(new Path(folder,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(folderTimestamp, f.find(folder).getModificationDate());
        assertEquals(folderEtag, f.find(folder).getETag());
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        assertEquals(test.attributes().getFileId(), attributes.getFileId());
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        finally {
            new CteraDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Path test = new CteraDirectoryFeature(session).mkdir(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final DAVAttributesFinderFeature f = new CteraAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(test);
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        assertEquals(test.attributes().getFileId(), attributes.getFileId());
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.file)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
    }
}
