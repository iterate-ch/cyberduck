package ch.cyberduck.core.dav;

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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.github.sardine.util.SardineUtil;

import static org.junit.Assert.*;

public class DAVMetadataFeatureTest extends AbstractDAVTest {

    @Test
    public void testSetMetadataFile() throws Exception {
        this.assumeNot(DAVTestServer.SFTPGO, "No support for custom properties");
        this.assumeNot(DAVTestServer.NGINX, "No support for PROPPATCH");
        final TransferStatus status = new TransferStatus();
        final Path test = new DAVTouchFeature(session).touch(new DAVWriteFeature(session), new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        final String v = new AlphanumericRandomStringService().random();
        final DAVMetadataFeature feature = new DAVMetadataFeature(session);
        feature.setMetadata(test, status.setMetadata(Collections.singletonMap("Test", v)));
        final Map<String, String> metadata = feature.getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Test"));
        assertEquals(v, metadata.get("Test"));
        test.attributes().setMetadata(metadata);
        feature.setMetadata(test, status.setMetadata(Collections.emptyMap()));
        assertFalse(feature.getMetadata(test).containsKey("Test"));
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testLegacyMetadata() throws Exception {
        this.assumeNot(DAVTestServer.SFTPGO, "No support for custom properties");
        this.assumeNot(DAVTestServer.NGINX, "No support for PROPPATCH");
        final Path test = new DAVTouchFeature(session).touch(new DAVWriteFeature(session), new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new DAVTimestampFeature(session).setTimestamp(test, new TransferStatus().setModified(1530305150000L));
        // Property with value for key written by previous versions
        final String v1 = new AlphanumericRandomStringService().random();
        final Element element = SardineUtil.createElement(SardineUtil.createQNameWithCustomNamespace("Legacy"));
        element.setTextContent(v1);
        final String path = new DAVPathEncoder(session).encode(test);
        session.getClient().patch(path, Collections.singletonList(element), Collections.emptyList());
        final DAVMetadataFeature feature = new DAVMetadataFeature(session);
        final Map<String, String> metadata = feature.getMetadata(test);
        // Timestamps in custom namespace are not metadata
        assertEquals(Collections.singletonMap("Legacy", v1), metadata);
        final Map<String, String> update = new HashMap<>(metadata);
        final String v2 = new AlphanumericRandomStringService().random();
        update.put("Test", v2);
        feature.setMetadata(test, new TransferStatus().setMetadata(update));
        // Migrated to single property found with attributes finder and listing
        this.assertMetadata(test, update);
        assertFalse(session.getClient().list(path, 0).get(0).getCustomPropsNS().containsKey(SardineUtil.createQNameWithCustomNamespace("Legacy")));
        assertEquals(1530305150000L, new DAVAttributesFinderFeature(session).find(test).getModificationDate());
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataFolder() throws Exception {
        this.assumeNot(DAVTestServer.SFTPGO, "No support for custom properties");
        this.assumeNot(DAVTestServer.NGINX, "No support for PROPPATCH");
        final TransferStatus status = new TransferStatus();
        final Path test = new DAVDirectoryFeature(session).mkdir(new DAVWriteFeature(session), new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), status);
        final String v = new AlphanumericRandomStringService().random();
        final DAVMetadataFeature feature = new DAVMetadataFeature(session);
        feature.setMetadata(test, status.setMetadata(Collections.singletonMap("Test", v)));
        final Map<String, String> metadata = feature.getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Test"));
        assertEquals(v, metadata.get("Test"));
        assertEquals(status.getMetadata(), metadata);
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testMetadataRetainedOnUpdate() throws Exception {
        this.assumeNot(DAVTestServer.SFTPGO, "No support for custom properties");
        this.assumeNot(DAVTestServer.NGINX, "No support for PROPPATCH");
        final Path test = new DAVTouchFeature(session).touch(new DAVWriteFeature(session), new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DAVMetadataFeature feature = new DAVMetadataFeature(session);
        final String v1 = new AlphanumericRandomStringService().random();
        feature.setMetadata(test, new TransferStatus().setMetadata(Collections.singletonMap("Test1", v1)));
        test.attributes().setMetadata(feature.getMetadata(test));
        final Map<String, String> update = new HashMap<>(test.attributes().getMetadata());
        final String v2 = new AlphanumericRandomStringService().random();
        update.put("Test2", v2);
        feature.setMetadata(test, new TransferStatus().setMetadata(update));
        final Map<String, String> expected = new HashMap<>();
        expected.put("Test1", v1);
        expected.put("Test2", v2);
        this.assertMetadata(test, expected);
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testMetadataRetainedOnOverwrite() throws Exception {
        this.assumeNot(DAVTestServer.SFTPGO, "No support for custom properties");
        this.assumeNot(DAVTestServer.NGINX, "No support for PROPPATCH");
        final Path test = new DAVTouchFeature(session).touch(new DAVWriteFeature(session), new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DAVMetadataFeature feature = new DAVMetadataFeature(session);
        final String v = new AlphanumericRandomStringService().random();
        feature.setMetadata(test, new TransferStatus().setMetadata(Collections.singletonMap("Test", v)));
        final byte[] content = RandomUtils.nextBytes(128);
        final TransferStatus status = new TransferStatus().setLength(content.length).setExists(true);
        final HttpResponseOutputStream<Void> out = new DAVWriteFeature(session).write(test, status, ConnectionCallback.noop);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertEquals(content.length, new DAVAttributesFinderFeature(session).find(test).getSize());
        this.assertMetadata(test, Collections.singletonMap("Test", v));
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testMetadataRetainedOnMove() throws Exception {
        this.assumeNot(DAVTestServer.SFTPGO, "No support for custom properties");
        this.assumeNot(DAVTestServer.NGINX, "No support for PROPPATCH");
        final Path home = new DefaultHomeFinderService(session).find();
        final Path test = new DAVTouchFeature(session).touch(new DAVWriteFeature(session), new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DAVMetadataFeature feature = new DAVMetadataFeature(session);
        final String v = new AlphanumericRandomStringService().random();
        feature.setMetadata(test, new TransferStatus().setMetadata(Collections.singletonMap("Test", v)));
        final Path target = new DAVMoveFeature(session).move(test, new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)),
                new TransferStatus(), new Delete.DisabledCallback(), ConnectionCallback.noop);
        this.assertMetadata(target, Collections.singletonMap("Test", v));
        new DAVDeleteFeature(session).delete(Collections.singletonList(target), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testMetadataRetainedOnCopy() throws Exception {
        this.assumeNot(DAVTestServer.SFTPGO, "No support for custom properties");
        this.assumeNot(DAVTestServer.NGINX, "No support for PROPPATCH");
        final Path home = new DefaultHomeFinderService(session).find();
        final Path test = new DAVTouchFeature(session).touch(new DAVWriteFeature(session), new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DAVMetadataFeature feature = new DAVMetadataFeature(session);
        final String v = new AlphanumericRandomStringService().random();
        feature.setMetadata(test, new TransferStatus().setMetadata(Collections.singletonMap("Test", v)));
        final Path copy = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DAVCopyFeature(session).copy(test, copy, new TransferStatus(), ConnectionCallback.noop, StreamListener.noop);
        this.assertMetadata(test, Collections.singletonMap("Test", v));
        this.assertMetadata(copy, Collections.singletonMap("Test", v));
        new DAVDeleteFeature(session).delete(Arrays.asList(test, copy), LoginCallback.noop, new Delete.DisabledCallback());
    }

    /**
     * Assert metadata is found with attributes finder and listing of parent directory
     */
    private void assertMetadata(final Path file, final Map<String, String> expected) throws Exception {
        final Map<String, String> metadata = new DAVMetadataFeature(session).getMetadata(file);
        assertEquals(expected, metadata);
        assertEquals(metadata, new DAVAttributesFinderFeature(session).find(file).getMetadata());
        final Path found = new DAVListService(session).list(file.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(file));
        assertNotNull(found);
        assertEquals(metadata, found.attributes().getMetadata());
    }

    @Test
    public void testComparisonAfterSetMetadata() throws Exception {
        this.assumeNot(DAVTestServer.SFTPGO, "No support for custom properties");
        this.assumeNot(DAVTestServer.NGINX, "No support for PROPPATCH");
        final Path test = new DAVTouchFeature(session).touch(new DAVWriteFeature(session),
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DAVListService(session).features(test.getParent()).contains(ListService.Flags.metadata));
        final PathAttributes before = new DAVListService(session).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test)).attributes();
        new DAVMetadataFeature(session).setMetadata(test.withAttributes(before), Collections.singletonMap("user_tags", "Red"));
        final Path found = new DAVListService(session).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test));
        assertEquals("Red", found.attributes().getMetadata().get("user_tags"));
        // Change of metadata only not detected by comparison service. Requires comparing metadata from listing
        assertEquals(Comparison.equal, session.getFeature(ComparisonService.class).compare(Path.Type.file, before, found.attributes()));
        new DAVDeleteFeature(session).delete(Collections.singletonList(found), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
