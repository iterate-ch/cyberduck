package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.EnumSet;

import ch.iterate.openstack.swift.model.Region;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SwiftRegionServiceTest extends AbstractTestCase {

    @Test
    public void testLookup() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final TransferStatus status = new TransferStatus();
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Region lookup = new SwiftRegionService(session).lookup((String) null);
        assertTrue(lookup.isDefault());
        assertEquals("DFW", lookup.getRegionId());
        assertNotNull(lookup.getCDNManagementUrl());
        assertNotNull(lookup.getStorageUrl());
    }

    @Test
    public void testLookupHp() throws Exception {
        final SwiftProtocol protocol = new SwiftProtocol() {
            @Override
            public String getContext() {
                return "/v2.0/tokens";
            }
        };
        final Host host = new Host(protocol, "region-a.geo-1.identity.hpcloudsvc.com", 35357);
        host.setCredentials(properties.getProperty("hpcloud.key"), properties.getProperty("hpcloud.secret"));
        final SwiftSession session = new SwiftSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final Region lookup = new SwiftRegionService(session).lookup((String) null);
        assertEquals("region-a.geo-1", lookup.getRegionId());
        assertNotNull(lookup.getStorageUrl());
        assertNotNull(lookup.getCDNManagementUrl());
    }
}
