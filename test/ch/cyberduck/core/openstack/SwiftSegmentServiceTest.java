package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import ch.iterate.openstack.swift.model.StorageObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SwiftSegmentServiceTest extends AbstractTestCase {

    @Test
    public void testList() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("/test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        container.attributes().setRegion("DFW");
        assertTrue(new SwiftSegmentService(session).list(new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE)).isEmpty());
        session.close();
    }

    @Test
    public void testManifest() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        final SwiftSegmentService service = new SwiftSegmentService(session);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final StorageObject a = new StorageObject("a");
        a.setMd5sum("m1");
        a.setSize(1L);
        final StorageObject b = new StorageObject("b");
        b.setMd5sum("m2");
        b.setSize(1L);
        final String manifest = service.manifest(container.getName(), Arrays.asList(a, b));
        assertEquals("[{\"size_bytes\":1,\"etag\":\"m1\",\"path\":\"\\/test.cyberduck.ch\\/a\"},{\"size_bytes\":1,\"etag\":\"m2\",\"path\":\"\\/test.cyberduck.ch\\/b\"}]", manifest);
    }
}
