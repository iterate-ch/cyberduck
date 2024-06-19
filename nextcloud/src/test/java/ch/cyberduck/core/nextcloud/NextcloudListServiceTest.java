package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.dav.DAVTouchFeature;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class NextcloudListServiceTest extends AbstractNextcloudTest {

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        new NextcloudListService(session).list(new Path(new DefaultHomeFinderService(session).find(),
                        new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testListFileException() throws Exception {
        final Path test = new DAVTouchFeature(new NextcloudWriteFeature(session)).touch(new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        try {
            final AttributedList<Path> list = new NextcloudListService(session).list(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory, Path.Type.volume)),
                    new DisabledListProgressListener());
        }
        finally {
            new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testList() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path directory = new DAVDirectoryFeature(session, new NextcloudAttributesFinderFeature(session)).mkdir(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final PathAttributes directoryAttributes = new DAVAttributesFinderFeature(session).find(directory);
        final String folderEtag = directoryAttributes.getETag();
        final long folderTimestamp = directoryAttributes.getModificationDate();
        final Path test = new DAVTouchFeature(new NextcloudWriteFeature(session)).touch(new Path(directory,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(Protocol.DirectoryTimestamp.implicit, session.getHost().getProtocol().getDirectoryTimestamp());
        assertNotEquals(folderTimestamp, new DAVAttributesFinderFeature(session).find(directory).getModificationDate());
        assertNotEquals(folderEtag, new DAVAttributesFinderFeature(session).find(directory).getETag());
        try {
            final AttributedList<Path> list = new NextcloudListService(session).list(directory,
                    new DisabledListProgressListener());
            assertEquals(1, list.size());
            assertNotNull(list.find(new SimplePathPredicate(test)));
            assertNotNull(list.find(new SimplePathPredicate(test)).attributes().getFileId());
        }
        finally {
            new DAVDeleteFeature(session).delete(Arrays.asList(test, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }
}
