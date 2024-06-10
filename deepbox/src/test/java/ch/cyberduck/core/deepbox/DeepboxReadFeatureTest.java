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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DeepboxReadFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testRead() throws Exception {
        final TransferStatus status = new TransferStatus();
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path file = new Path("/Mountainduck Buddies/My Box/Documents/Auditing/nix4.txt", EnumSet.of(AbstractPath.Type.file));
        final String s = IOUtils.toString(new DeepboxReadFeature(session, nodeid).read(file, status, new DisabledConnectionCallback()));
        assertNotNull(s);
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new DeepboxDirectoryFeature(session, nodeid).mkdir(
                new Path(auditing, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        try {
            new DeepboxReadFeature(session, nodeid).read(new Path(folder, "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
        }
        finally {
            new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }
}
