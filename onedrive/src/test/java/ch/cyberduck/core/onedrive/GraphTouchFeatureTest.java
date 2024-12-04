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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.onedrive.features.GraphWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GraphTouchFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testTouch() throws Exception {
        final Path file = new GraphTouchFeature(session, fileid).touch(new Path(new OneDriveHomeFinderService().find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(file.attributes().getFileId(), new GraphAttributesFinderFeature(session, fileid).find(file).getFileId());
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchUmlaut() throws Exception {
        final Path file = new Path(new OneDriveHomeFinderService().find(), String.format("%sä", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        new GraphTouchFeature(session, fileid).touch(file, new TransferStatus());
        assertNotNull(new GraphAttributesFinderFeature(session, fileid).find(file));
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTouchEqualSign() throws Exception {
        final Path file = new Path(new OneDriveHomeFinderService().find(), String.format("%s====", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        new GraphTouchFeature(session, fileid).touch(file, new TransferStatus());
        assertNotNull(new GraphAttributesFinderFeature(session, fileid).find(file));
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWhitespaceTouch() throws Exception {
        final RandomStringService randomStringService = new AlphanumericRandomStringService();
        final Path file = new Path(new OneDriveHomeFinderService().find(), String.format("%s %s", randomStringService.random(), randomStringService.random()), EnumSet.of(Path.Type.file));
        new GraphTouchFeature(session, fileid).touch(file, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(new GraphAttributesFinderFeature(session, fileid).find(file));
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        final Path container = new OneDriveHomeFinderService().find();
        final String filename = StringUtils.lowerCase(new AlphanumericRandomStringService().random());
        final Path file = new GraphTouchFeature(session, fileid)
                .touch(new Path(container, filename, EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        final byte[] content = RandomUtils.nextBytes(254);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final StatusOutputStream<DriveItem.Metadata> out = new GraphWriteFeature(session, fileid).write(
                new Path(container, StringUtils.upperCase(filename), EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        final AttributedList<Path> list = new OneDriveListService(session, fileid).list(container, new DisabledListProgressListener());
        assertNotNull(list.find(new SimplePathPredicate(new Path(container, StringUtils.upperCase(filename), EnumSet.of(Path.Type.file)))));
        assertEquals(content.length, list.find(new SimplePathPredicate(new Path(container, StringUtils.upperCase(filename), EnumSet.of(Path.Type.file)))).attributes().getSize());
        assertNull(list.find(new SimplePathPredicate(file)));
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
