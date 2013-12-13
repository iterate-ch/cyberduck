package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import ch.iterate.openstack.swift.model.Region;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class SwiftRegionServiceTest extends AbstractTestCase {

    @Test
    public void testLookup() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final TransferStatus status = new TransferStatus();
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Region lookup = new SwiftRegionService(session).lookup((String) null);
        assertTrue(lookup.isDefault());
        assertEquals("DFW", lookup.getRegionId());
    }
}
