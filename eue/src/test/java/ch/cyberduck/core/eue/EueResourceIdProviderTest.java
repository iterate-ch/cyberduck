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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.AbstractPath.Type.directory;
import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class EueResourceIdProviderTest extends AbstractEueSessionTest {

    @Test
    public void getFileIdRoot() throws Exception {
        assertEquals(EueResourceIdProvider.ROOT, new EueResourceIdProvider(session).getFileId(
                new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void getFileIdTrash() throws Exception {
        assertEquals(EueResourceIdProvider.TRASH, new EueResourceIdProvider(session).getFileId(new Path("Gelöschte Dateien", EnumSet.of(directory))));
    }

    @Test
    public void testParseResourceUri() throws Exception {
        assertEquals("1030364733607248477", EueResourceIdProvider.getResourceIdFromResourceUri("../../resource/1030364733607248477"));
        assertEquals("1030365219475424239", EueResourceIdProvider.getResourceIdFromResourceUri("1030365219475424239"));
    }

    @Test
    public void testFindCaseInsensitive() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path folder = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        folder.withAttributes(new EueAttributesFinderFeature(session, fileid).find(folder));
        assertEquals(folder.attributes().getFileId(), fileid.getFileId(folder));
        assertEquals(folder.attributes().getFileId(), fileid.getFileId(new Path(StringUtils.lowerCase(folder.getAbsolute()), folder.getType())));
        assertEquals(folder.attributes().getFileId(), fileid.getFileId(new Path(StringUtils.upperCase(folder.getAbsolute()), folder.getType())));
        final Path file = createFile(fileid, new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), RandomUtils.nextBytes(124));
        fileid.clear();
        assertEquals(file.attributes().getFileId(), fileid.getFileId(file));
        assertEquals(file.attributes().getFileId(), fileid.getFileId(new Path(StringUtils.lowerCase(file.getAbsolute()), file.getType())));
        assertEquals(file.attributes().getFileId(), fileid.getFileId(new Path(StringUtils.upperCase(file.getAbsolute()), file.getType())));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}