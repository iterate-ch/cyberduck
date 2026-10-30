package ch.cyberduck.core.openstack;

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import ch.iterate.openstack.swift.model.StorageObject;

import static org.junit.Assert.*;

public class SwiftReadFeatureTest extends AbstractSwiftTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final SwiftRegionService regionService = new SwiftRegionService(session);
        new SwiftReadFeature(session, regionService).read(new Path(container, "nosuchname", EnumSet.of(Path.Type.file)), status, ConnectionCallback.noop);
    }

    @Test
    public void testReadRange() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(new SwiftWriteFeature(session, new SwiftRegionService(session)), test, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(1023);
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final HttpResponseOutputStream<StorageObject> out = new SwiftWriteFeature(session, regionService).write(test, new TransferStatus().setLength(content.length), ConnectionCallback.noop);
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertNotNull(out.getStatus());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new SwiftReadFeature(session, regionService).read(test, status, ConnectionCallback.noop);
        assertNotNull(in);
        assertEquals(content.length, status.getLength(), 0L);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testReadRangeUnknownLength() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(new SwiftWriteFeature(session, new SwiftRegionService(session)), test, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(1023);
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final HttpResponseOutputStream<StorageObject> out = new SwiftWriteFeature(session, regionService).write(test, new TransferStatus().setLength(content.length), ConnectionCallback.noop);
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertNotNull(out.getStatus());
        final TransferStatus status = new TransferStatus();
        // Set to unknown length
        status.setLength(-1L);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new SwiftReadFeature(session, regionService).read(test, status, ConnectionCallback.noop);
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
