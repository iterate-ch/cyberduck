package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import ch.iterate.openstack.swift.model.StorageObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SwiftSegmentServiceTest {

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("/test.cyberduck.ch", EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("DFW");
        assertTrue(new SwiftSegmentService(session).list(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))).isEmpty());
        session.close();
    }

    @Test
    public void testManifest() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        final SwiftSegmentService service = new SwiftSegmentService(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final StorageObject a = new StorageObject("a");
        a.setMd5sum("m1");
        a.setSize(1L);
        final StorageObject b = new StorageObject("b");
        b.setMd5sum("m2");
        b.setSize(1L);
        final String manifest = service.manifest(container.getName(), Arrays.asList(a, b));
        assertEquals("[{\"path\":\"/test.cyberduck.ch/a\",\"etag\":\"m1\",\"size_bytes\":1},{\"path\":\"/test.cyberduck.ch/b\",\"etag\":\"m2\",\"size_bytes\":1}]", manifest);
    }

    @Test
    public void testChecksum() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        final SwiftSegmentService service = new SwiftSegmentService(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, "a", EnumSet.of(Path.Type.file));
        final StorageObject a = new StorageObject("a");
        a.setMd5sum("m1");
        a.setSize(1L);
        final StorageObject b = new StorageObject("b");
        b.setMd5sum("m2");
        b.setSize(1L);
        final Checksum checksum = service.checksum(new MD5ChecksumCompute(), Arrays.asList(a, b));
        assertEquals(new MD5ChecksumCompute().compute(IOUtils.toInputStream("m1m2", Charset.defaultCharset()), new TransferStatus()), checksum);
    }

    @Test
    public void testBasename() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        final SwiftSegmentService service = new SwiftSegmentService(session, ".prefix/");
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString() + "/" + UUID.randomUUID().toString();
        assertEquals(".prefix/" + name + "/3", service.basename(new Path(container, name, EnumSet.of(Path.Type.file)), 3L));
    }
}
