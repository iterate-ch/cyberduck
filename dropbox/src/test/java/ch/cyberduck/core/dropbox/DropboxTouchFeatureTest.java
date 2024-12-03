package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxTouchFeatureTest extends AbstractDropboxTest {

    @Test(expected = AccessDeniedException.class)
    public void testDisallowedName() throws Exception {
        final DropboxTouchFeature touch = new DropboxTouchFeature(session);
        final Path file = new Path(new DefaultHomeFinderService(session).find(), String.format("~%s.tmp", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        assertFalse(touch.isSupported(new DefaultHomeFinderService(session).find(), file.getName()));
        touch.touch(file, new TransferStatus());
    }

    @Test
    public void testFindFile() throws Exception {
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DropboxTouchFeature(session).touch(file, new TransferStatus());
        assertTrue(new DropboxFindFeature(session).find(file));
        assertTrue(new DefaultFindFeature(session).find(file));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCaseSensitivity() throws Exception {
        final Path container = new DefaultHomeFinderService(session).find();
        final String filename = StringUtils.lowerCase(new AlphanumericRandomStringService().random());
        final Path file = new DropboxTouchFeature(session)
                .touch(new Path(container, filename, EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        new DropboxTouchFeature(session)
                .touch(new Path(container, StringUtils.upperCase(filename), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        final byte[] content = RandomUtils.nextBytes(254);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final StatusOutputStream out = new DropboxWriteFeature(session).write(
                new Path(container, StringUtils.upperCase(filename), EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        final AttributedList<Path> list = new DropboxListService(session).list(container, new DisabledListProgressListener());
        assertNotNull(list.find(new SimplePathPredicate(new Path(container, StringUtils.upperCase(filename), EnumSet.of(Path.Type.file)))));
        assertEquals(content.length, list.find(new SimplePathPredicate(new Path(container, StringUtils.upperCase(filename), EnumSet.of(Path.Type.file)))).attributes().getSize());
        assertNull(list.find(new SimplePathPredicate(file)));
        new DropboxDeleteFeature(session).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
