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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;


@Category(IntegrationTest.class)
public class DeepboxListServiceTest extends AbstractDeepboxTest {
    @Test
    public void testListDeepBoxes() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(directory, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertEquals(1, list.size());
        for(final Path f : list) {
            assertSame(directory, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            // no modification/creation date for DeepBoxes
            assertTrue(f.attributes().getModificationDate() < 0);
            assertTrue(f.attributes().getCreationDate() < 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testListBoxes() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(deepBox, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertEquals(1, list.size());
        for(final Path f : list) {
            assertSame(deepBox, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            // no modification/creation date for Boxes
            assertTrue(f.attributes().getModificationDate() < 0);
            assertTrue(f.attributes().getCreationDate() < 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testListMyBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(box, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box/Inbox", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertEquals(3, list.size());
        for(final Path f : list) {
            assertSame(box, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            // no modification/creation date for Inbox/Documents/Trash virtual folder level
            assertTrue(f.attributes().getModificationDate() < 0);
            assertTrue(f.attributes().getCreationDate() < 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testListDocuments() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(documents, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box/Documents/Auditing", EnumSet.of(Path.Type.directory)))));
        assertEquals(13, list.size());
        for(final Path f : list) {
            assertSame(documents, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            assertTrue(f.attributes().getModificationDate() > 0);
            assertTrue(f.attributes().getCreationDate() > 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testListInbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path queue = new Path("/Mountainduck Buddies/My Box/Inbox", EnumSet.of(Path.Type.directory));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(queue, new DisabledListProgressListener());
        assertNotEquals(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        for(final Path f : list) {
            assertSame(queue, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            assertTrue(f.attributes().getModificationDate() > 0);
            assertTrue(f.attributes().getCreationDate() > 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testListTrash() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path trash = new Path("/Mountainduck Buddies/My Box/Trash", EnumSet.of(Path.Type.directory));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(trash, new DisabledListProgressListener());
        assertNotEquals(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        for(final Path f : list) {
            assertSame(trash, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            assertTrue(f.attributes().getModificationDate() > 0);
            assertTrue(f.attributes().getCreationDate() > 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            //assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }


    @Test
    public void testListAuditing() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);

        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(
                auditing, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());

        assertNotNull(list.find(new SimplePathPredicate(new Path("/Mountainduck Buddies/My Box/Documents/Auditing/nix4.txt", EnumSet.of(Path.Type.file)))));
        assertEquals(1, list.size());
        for(final Path f : list) {
            assertSame(auditing, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            assertTrue(f.attributes().getModificationDate() > 0);
            assertTrue(f.attributes().getCreationDate() > 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testChunksizeExact() throws Exception {
        session.getHost().setProperty("deepbox.listing.chunksize", "5");
        final int chunkSize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path(auditing, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());

        final int numFiles = chunkSize * 2;
        for(int i = 0; i < numFiles; ++i) {
            new DeepboxTouchFeature(session, nodeid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file)), new TransferStatus());
        }
        final AttributedList<Path> listing = new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener());
        assertEquals(numFiles, listing.size());
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testChunksizeInexact() throws Exception {
        // TODO check this is effective
        session.getHost().setProperty("deepbox.listing.chunksize", "5");
        final int chunkSize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path(auditing, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());

        final int numFiles = (int) Math.floor(chunkSize * 2.5);
        for(int i = 0; i < numFiles; ++i) {
            new DeepboxTouchFeature(session, nodeid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file)), new TransferStatus());
        }
        final AttributedList<Path> listing = new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener());
        assertEquals(numFiles, listing.size());
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDuplicatesListing() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path folder = new Path(auditing, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());

        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file));
        for(int i = 0; i < 2; i++) {
            final HttpEntityEnclosingRequestBase request = new HttpPost(String.format("%s/api/v1/deepBoxes/%s/boxes/%s/files/%s",
                    session.getClient().getBasePath(),
                    nodeid.getDeepBoxNodeId(folder),
                    nodeid.getBoxNodeId(folder),
                    nodeid.getFileId(folder)));

            final MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(new AlphanumericRandomStringService().random().getBytes(StandardCharsets.UTF_8));
            multipart.addBinaryBody("files", out.toByteArray(), ContentType.APPLICATION_OCTET_STREAM, file.getName());
            request.setEntity(multipart.build());
            CloseableHttpResponse execute = session.getClient().getClient().execute(request);
            assertEquals(201, execute.getStatusLine().getStatusCode());
        }

        final CoreRestControllerApi core = new CoreRestControllerApi(session.getClient());
        final NodeContent nodeContent = core.listNodeContent(UUID.fromString(nodeid.getFileId(folder)), null, null, "modifiedTime desc");
        assertEquals(2, nodeContent.getNodes().size());
        final AttributedList<Path> listing = new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener());
        assertEquals(1, listing.size());
        assertEquals(nodeContent.getNodes().get(0).getNodeId().toString(), listing.get(0).attributes().getFileId());
        assertEquals(nodeContent.getNodes().get(0).getNodeId().toString(), nodeid.getFileId(file));
        new DeepboxDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}