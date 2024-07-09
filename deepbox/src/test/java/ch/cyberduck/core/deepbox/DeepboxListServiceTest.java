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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.api.PathRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Folder;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeCopy;
import ch.cyberduck.core.features.FileIdProvider;
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
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path directory = new Path("/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(directory, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        assertNotNull(list.find(new SimplePathPredicate(new Path("/ORG 4 - DeepBox Desktop App", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertEquals(2, list.size());
        for(final Path f : list) {
            assertSame(directory, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            // no modification/creation date for DeepBoxes
            assertTrue(f.attributes().getModificationDate() < 0);
            assertTrue(f.attributes().getCreationDate() < 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(new Path(f.getAbsolute(), f.getType())));
        }
    }

    @Test
    public void testListBoxes() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path deepBox = new Path("/ORG 4 - DeepBox Desktop App/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(deepBox, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        assertNotNull(list.find(new SimplePathPredicate(new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1", EnumSet.of(Path.Type.directory, Path.Type.volume)))));
        assertEquals(2, list.size());
        for(final Path f : list) {
            assertSame(deepBox, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            // no modification/creation date for Boxes
            assertTrue(f.attributes().getModificationDate() < 0);
            assertTrue(f.attributes().getCreationDate() < 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(new Path(f.getAbsolute(), f.getType())));
        }
    }

    @Test
    public void testListBox() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path box = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(box, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        final Path inbox = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Inbox", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertNotNull(list.find(new SimplePathPredicate(inbox)));
        assertEquals("dc37e9db-36e9-4330-881c-730789aaa8ce", nodeid.getFileId(inbox));
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertNotNull(list.find(new SimplePathPredicate(documents)));
        assertEquals("ec5f9666-f99e-47ad-bc8c-41da9f1324e2", nodeid.getFileId(documents));
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertNotNull(list.find(new SimplePathPredicate(trash)));
        assertEquals("1fc77175-f2a7-4b65-bd38-9aaeb9272a90", nodeid.getFileId(trash));
        assertEquals(3, list.size());
        for(final Path f : list) {
            assertSame(box, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            // no modification/creation date for Inbox/Documents/Trash virtual folder level
            assertTrue(f.attributes().getModificationDate() < 0);
            assertTrue(f.attributes().getCreationDate() < 0);
            final String fileId = nodeid.getFileId(f);
            assertEquals(fileId, f.attributes().getFileId());
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(f));
        }
    }

    @Test
    public void testListDocuments() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path documents = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(documents, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        assertNotNull(list.find(new SimplePathPredicate(new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Taxes", EnumSet.of(Path.Type.directory)))));
        for(final Path f : list) {
            assertSame(documents, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            assertTrue(f.attributes().getModificationDate() > 0);
            assertTrue(f.attributes().getCreationDate() > 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(new Path(f.getAbsolute(), f.getType())));
        }
    }

    @Test
    public void testListInbox() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path queue = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Inbox", EnumSet.of(Path.Type.directory));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(queue, new DisabledListProgressListener());
        assertNotEquals(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());
        for(final Path f : list) {
            assertSame(queue, f.getParent());
            assertFalse(f.getName().contains(String.valueOf(Path.DELIMITER)));
            assertTrue(f.attributes().getModificationDate() > 0);
            assertTrue(f.attributes().getCreationDate() > 0);
            assertNotNull(nodeid.getFileId(new Path(f).withAttributes(PathAttributes.EMPTY)));
            assertEquals(f.attributes(), new DeepboxAttributesFinderFeature(session, nodeid).find(new Path(f.getAbsolute(), f.getType())));
        }
    }

    @Test
    public void testListTrash() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path trash = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash", EnumSet.of(Path.Type.directory));
        new DeepboxListService(session, nodeid).list(trash, new DisabledListProgressListener()); // assert no fail
    }

    @Test
    public void testListReceipts() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path receipts = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Invoices : Receipts/", EnumSet.of(AbstractPath.Type.directory));
        final AttributedList<Path> list = new DeepboxListService(session, nodeid).list(receipts, new DisabledListProgressListener());
        assertNotSame(AttributedList.emptyList(), list);
        assertFalse(list.isEmpty());

        assertNotNull(list.find(new SimplePathPredicate(new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Invoices : Receipts/RE-IN - Copy1.pdf", EnumSet.of(Path.Type.file)))));
        assertNotNull(list.find(new SimplePathPredicate(new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Invoices : Receipts/RE-IN - Copy2.pdf", EnumSet.of(Path.Type.file)))));
        assertEquals(2, list.size());
        for(final Path f : list) {
            assertSame(receipts, f.getParent());
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
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);

        final Path receipts = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Invoices : Receipts/", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(receipts, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());
        try {
            final int numFiles = chunkSize * 2;
            for(int i = 0; i < numFiles; ++i) {
                new DeepboxTouchFeature(session, nodeid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file)), new TransferStatus());
            }
            final AttributedList<Path> listing = new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener());
            assertEquals(numFiles, listing.size());
        }
        finally {
            deleteAndPurge(folder);
        }
    }

    @Test
    public void testChunksizeInexact() throws Exception {
        session.getHost().setProperty("deepbox.listing.chunksize", "5");
        final int chunkSize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);

        final Path receipts = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Invoices : Receipts/", EnumSet.of(AbstractPath.Type.directory));
        final Path folder = new Path(receipts, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());
        try {
            final int numFiles = (int) Math.floor(chunkSize * 2.5);
            for(int i = 0; i < numFiles; ++i) {
                new DeepboxTouchFeature(session, nodeid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file)), new TransferStatus());
            }
            final AttributedList<Path> listing = new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener());
            assertEquals(numFiles, listing.size());
        }
        finally {
            deleteAndPurge(folder);
        }
    }

    @Test
    public void testDuplicatesListing() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);

        final Path auditing = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Invoices : Receipts/", EnumSet.of(AbstractPath.Type.directory));
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
        assertEquals(0, listing.size());
        assertTrue(nodeContent.getNodes().get(0).getNodeId().toString().equals(nodeid.getFileId(file)) ||
                nodeContent.getNodes().get(1).getNodeId().toString().equals(nodeid.getFileId(file))
        );
        deleteAndPurge(folder);
    }

    @Test
    public void testDuplicateFiles() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path virtualFolder = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Bookkeeping", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(virtualFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file));
        new DeepboxTouchFeature(session, nodeid).touch(file, new TransferStatus());

        final NodeCopy body = new NodeCopy();
        body.setTargetParentNodeId(UUID.fromString(nodeid.getFileId(folder)));
        new CoreRestControllerApi(session.getClient()).copyNode(body, UUID.fromString(nodeid.getFileId(file)));
        final NodeContent remote = new CoreRestControllerApi(session.getClient()).listNodeContent(UUID.fromString(nodeid.getFileId(folder)), 0, 50, null);
        assertEquals(2, remote.getNodes().size());
        try {
            assertEquals(0, new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener()).size());
        }
        finally {
            deleteAndPurge(folder);
        }
    }

    @Test
    public void testDuplicateFolders() throws Exception {
        final DeepboxIdProvider nodeid = (DeepboxIdProvider) session.getFeature(FileIdProvider.class);
        final Path virtualFolder = new Path("/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Bookkeeping", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(virtualFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        new DeepboxDirectoryFeature(session, nodeid).mkdir(test, new TransferStatus());

        // /api/v1/nodes/{nodeId}/copy does not work for folders
        final Folder body = new Folder();
        body.setName(test.getName());
        new PathRestControllerApi(session.getClient()).addFolders(
                Collections.singletonList(body),
                UUID.fromString(nodeid.getDeepBoxNodeId(test)),
                UUID.fromString(nodeid.getBoxNodeId(test)),
                UUID.fromString(nodeid.getFileId(test.getParent()))
        );

        final NodeContent remote = new CoreRestControllerApi(session.getClient()).listNodeContent(UUID.fromString(nodeid.getFileId(folder)), 0, 50, null);
        assertEquals(2, remote.getNodes().size());
        try {
            assertEquals(0, new DeepboxListService(session, nodeid).list(folder, new DisabledListProgressListener()).size());
        }
        finally {
            deleteAndPurge(folder);
        }
    }
}