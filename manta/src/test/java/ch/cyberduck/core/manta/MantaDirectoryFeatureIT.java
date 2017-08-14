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
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class MantaDirectoryFeatureIT extends AbstractMantaTest {

    @Test
    public void testMkdir() throws Exception {
        final Path target = new MantaDirectoryFeature(session).mkdir(randomDirectory(), null, null);
        final PathAttributes found = new MantaAttributesFinderFeature(session).find(target);
        assertEquals(found.getDisplayname(), target.getName());
        new MantaDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWhitespaceMkdir() throws Exception {
        final RandomStringService randomStringService = new AlphanumericRandomStringService();
        final Path target = new MantaDirectoryFeature(session)
                .mkdir(
                        new Path(
                                testPathPrefix,
                                String.format("%s %s", randomStringService.random(), randomStringService.random()),
                                EnumSet.of(Path.Type.directory)
                        ), null, null);
        final Attributes found = new MantaAttributesFinderFeature(session).find(target);
        assertNotNull(found.getOwner());
        assertNotNull(found.getCreationDate());
        new MantaDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
