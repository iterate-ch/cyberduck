package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import ch.iterate.openstack.swift.io.ContentLengthInputStream;
import ch.iterate.openstack.swift.model.StorageObject;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftReadFeatureTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final TransferStatus status = new TransferStatus();
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final SwiftRegionService regionService = new SwiftRegionService(session);
        new SwiftReadFeature(session, regionService).read(new Path(container, "nosuchname", EnumSet.of(Path.Type.file)), status);
    }

    @Test
    public void testReadRange() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session).touch(test, new TransferStatus());
        final byte[] content = RandomStringUtils.random(1000).getBytes();
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final HttpResponseOutputStream<StorageObject> out = new SwiftWriteFeature(session, regionService).write(test, new TransferStatus().length(content.length));
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertNotNull(out.getResponse());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new SwiftReadFeature(session, regionService).read(test, status);
        assertNotNull(in);
        assertTrue(in instanceof ContentLengthInputStream);
        assertEquals(content.length - 100, ((ContentLengthInputStream) in).getLength(), 0L);
        assertEquals(content.length, status.getLength(), 0L);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testReadRangeUnknownLength() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session).touch(test, new TransferStatus());
        final byte[] content = RandomStringUtils.random(1000).getBytes();
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final HttpResponseOutputStream<StorageObject> out = new SwiftWriteFeature(session, regionService).write(test, new TransferStatus().length(content.length));
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertNotNull(out.getResponse());
        final TransferStatus status = new TransferStatus();
        // Set to unknown length
        status.setLength(-1L);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new SwiftReadFeature(session, regionService).read(test, status);
        assertNotNull(in);
        assertTrue(in instanceof ContentLengthInputStream);
        assertEquals(content.length - 100, ((ContentLengthInputStream) in).getLength(), 0L);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testDownloadGzip() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final TransferStatus status = new TransferStatus();
        status.setLength(182L);
        final Path container = new Path(".ACCESS_LOGS", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final InputStream in = new SwiftReadFeature(session, regionService).read(new Path(container,
                "/cdn.cyberduck.ch/2015/03/01/10/3b1d6998c430d58dace0c16e58aaf925.log.gz",
                EnumSet.of(Path.Type.file)), status);
        assertNotNull(in);
        new StreamCopier(status, status).transfer(in, new NullOutputStream());
        assertEquals(182L, status.getOffset());
        assertEquals(182L, status.getLength());
        in.close();
        session.close();
    }

    @Test
    public void testReadCloseReleaseEntity() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final TransferStatus status = new TransferStatus();
        final Path container = new Path(".ACCESS_LOGS", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final CountingInputStream in = new CountingInputStream(new SwiftReadFeature(session, regionService).read(new Path(container,
                "/cdn.cyberduck.ch/2015/03/01/10/3b1d6998c430d58dace0c16e58aaf925.log.gz",
                EnumSet.of(Path.Type.file)), status));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        session.close();
    }
}
