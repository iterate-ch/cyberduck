package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.util.EnumSet;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DropboxReadFeatureTest extends AbstractDropboxTest {

    @Test
    public void testReadInterrupt() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), "eetaik4R", EnumSet.of(Path.Type.file));
        // Unknown length in status
        final TransferStatus status = new TransferStatus();
        // Read a single byte
        {
            final InputStream in = new DropboxReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in.read());
            in.close();
        }
        {
            final InputStream in = new DropboxReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            in.close();
        }
        session.close();
    }
}