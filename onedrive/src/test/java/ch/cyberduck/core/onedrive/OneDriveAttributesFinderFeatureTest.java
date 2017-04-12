package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class OneDriveAttributesFinderFeatureTest extends AbstractOneDriveTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final OneDriveAttributesFinderFeature f = new OneDriveAttributesFinderFeature(session);
        try {
            f.find(new Path(UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file)));
        }
        catch(NotfoundException e) {
            assertEquals("Unexpected response (404 Not Found). Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testFindDrives() throws Exception {
        final Path file = new Path("/", EnumSet.of(Path.Type.directory));
        OneDriveAttributesFinderFeature attributesFinderFeature = new OneDriveAttributesFinderFeature(session);
        final AttributedList<Path> list = new OneDriveListService(session).list(file, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertNotNull(attributesFinderFeature.find(f));
        }
    }

    @Test
    public void testFindFiles() throws Exception {
        OneDriveAttributesFinderFeature attributesFinderFeature = new OneDriveAttributesFinderFeature(session);
        OneDriveListService listService = new OneDriveListService(session);
        final AttributedList<Path> list = listService.list(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            final AttributedList<Path> children = listService.list(f, new DisabledListProgressListener());
            for(Path c : children) {
                final PathAttributes attributes = attributesFinderFeature.find(c);
                assertNotNull(attributes);
                assertNotEquals(-1L, attributes.getSize());
                assertNotEquals(-1L, attributes.getCreationDate());
                assertNotEquals(-1L, attributes.getModificationDate());
                assertNotNull(attributes.getETag());
                assertNull(attributes.getVersionId());
                assertNotNull(attributes.getLink());
            }
        }
    }
}
