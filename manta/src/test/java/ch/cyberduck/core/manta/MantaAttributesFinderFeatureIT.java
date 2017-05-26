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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class MantaAttributesFinderFeatureIT extends AbstractMantaTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        try {
            new MantaAttributesFinderFeature(session).find(new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
        }
        catch(NotfoundException e) {
            assertEquals("Not Found. Item does not exist. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testFindFile() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new MantaTouchFeature(session).touch(file, new TransferStatus().withMime("x-application/cyberduck"));
        final PathAttributes attributes = new MantaAttributesFinderFeature(session).find(file);
        assertNotNull(attributes);
        assertNotEquals(-1L, attributes.getSize());
        assertNotEquals(-1L, attributes.getCreationDate());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getETag());
        assertNull(attributes.getVersionId());
        assertNotNull(attributes.getLink());
        new MantaDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
