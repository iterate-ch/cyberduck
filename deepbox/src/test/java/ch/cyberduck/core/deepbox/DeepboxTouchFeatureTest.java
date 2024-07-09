package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DeepboxTouchFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testTouchRoot() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        assertThrows(AccessDeniedException.class, () -> new DeepboxTouchFeature(session, fileid).preflight(Home.ROOT, new AlphanumericRandomStringService().random()));
        assertThrows(NotfoundException.class, () -> new DeepboxTouchFeature(session, fileid).touch(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus()));
    }

    @Test
    public void testNoDuplicates() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new DeepboxTouchFeature(session, fileid).touch(new Path(documents, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new DeepboxTouchFeature(session, fileid).preflight(documents.withAttributes(new DeepboxAttributesFinderFeature(session, fileid).find(documents)), test.getName());
        assertTrue(new DeepboxFindFeature(session, fileid).find(test));
        deleteAndPurge(test);
    }

    @Test
    public void testAccents() throws Exception {
        final DeepboxIdProvider fileid = new DeepboxIdProvider(session);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Insurance", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new DeepboxTouchFeature(session, fileid).touch(new Path(documents, new AlphanumericRandomStringService().random() + "éf", EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DeepboxFindFeature(session, fileid).find(documents));
        deleteAndPurge(test);
    }
}