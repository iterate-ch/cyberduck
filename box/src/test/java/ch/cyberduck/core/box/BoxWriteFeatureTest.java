package ch.cyberduck.core.box;

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
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class BoxWriteFeatureTest extends AbstractBoxTest {

    @Test
    public void testWrite() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final BoxWriteFeature feature = new BoxWriteFeature(session, fileid);
        final Path folder = new BoxDirectoryFeature(session, fileid).mkdir(
                new Path(Home.ROOT, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final long folderModification = new BoxAttributesFinderFeature(session, fileid).find(folder).getModificationDate();
        assertEquals(folderModification, folder.attributes().getModificationDate());
        // Makes sure to test overwrite
        final Path file = new BoxTouchFeature(session, fileid).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(2047);
        final TransferStatus status = new TransferStatus();
        status.setModified(1503654614004L); //GMT: Friday, 25. August 2017 09:50:14.004
        status.setLength(content.length);
        status.setExists(true);
        status.setRemote(file.attributes());
        status.setMime(MimeTypeService.DEFAULT_CONTENT_TYPE);
        status.setChecksum(feature.checksum(file, status).compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<File> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(progress, progress).withListener(count).transfer(in, out);
        assertEquals(content.length, count.getSent());
        in.close();
        out.close();
        assertNotNull(out.getStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        assertTrue(new BoxFindFeature(session, fileid).find(file));
        final PathAttributes attributes = new BoxAttributesFinderFeature(session, fileid).find(file);
        assertEquals(content.length, attributes.getSize());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new BoxReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        // Check folder attributes after write
        assertEquals(folderModification, new BoxAttributesFinderFeature(session, fileid).find(folder).getModificationDate(), 0L);
        final PathAttributes fileAttr = new BoxAttributesFinderFeature(session, fileid).find(file);
        assertNotEquals(file.attributes(), fileAttr);
        assertEquals(file.attributes().getCreationDate(), fileAttr.getCreationDate());
        assertNotEquals(file.attributes().getModificationDate(), fileAttr.getModificationDate());
        assertEquals(1503654614000L, fileAttr.getModificationDate()); //milliseconds are ignored by the Box - GMT: Friday, 25. August 2017 09:50:14
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
