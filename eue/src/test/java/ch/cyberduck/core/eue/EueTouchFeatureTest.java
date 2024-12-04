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
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

@Category(IntegrationTest.class)
public class EueTouchFeatureTest extends AbstractEueSessionTest {

    @Test
    public void testSupported() {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        assertFalse(new EueTouchFeature(session, fileid).isSupported(new Path("/", EnumSet.of(Path.Type.directory)), "f >f"));
        assertFalse(new EueTouchFeature(session, fileid).isSupported(new Path("/", EnumSet.of(Path.Type.directory)), "f "));
        assertFalse(new EueTouchFeature(session, fileid).isSupported(new Path("/", EnumSet.of(Path.Type.directory)), "f."));
        // max path length exceeded. Allowed are 250, but the length was 255
        assertFalse(new EueTouchFeature(session, fileid).isSupported(new Path("/", EnumSet.of(Path.Type.directory)),
                new AsciiRandomStringService(255).random()));
    }

    @Test
    public void testConflict() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path container = new EueDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new EueTouchFeature(session, fileid).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        // Create conflict
        assertThrows(ConflictException.class, () -> new EueTouchFeature(session, fileid).touch(file, new TransferStatus().withLength(0L)));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCaseSensitivity() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path container = new EueDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String filename = StringUtils.lowerCase(new AlphanumericRandomStringService().random());
        final Path file = new EueTouchFeature(session, fileid)
                .touch(new Path(container, filename, EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        // Create conflict
        assertThrows(ConflictException.class, () -> new EueTouchFeature(session, fileid)
                .touch(new Path(container, StringUtils.capitalize(filename), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L)));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
