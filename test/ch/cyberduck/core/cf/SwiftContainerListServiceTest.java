package ch.cyberduck.core.cf;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import com.rackspacecloud.client.cloudfiles.FilesContainer;
import com.rackspacecloud.client.cloudfiles.FilesRegion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SwiftContainerListServiceTest extends AbstractTestCase {

    @Test(expected = IOException.class)
    public void testListNoCredentials() throws Exception {
        new SwiftContainerListService().list(new CFSession(
                new Host(Protocol.CLOUDFILES, Protocol.CLOUDFILES.getDefaultHostname())));
    }

    @Test
    public void testList() throws Exception {
        final CFSession session = new CFSession(
                new Host(Protocol.CLOUDFILES, Protocol.CLOUDFILES.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.connect();
        final List<FilesContainer> list = new SwiftContainerListService().list(session);
        final CFPath container = new CFPath(session, "test.cyberduck.ch", Path.VOLUME_TYPE);
        assertFalse(list.contains(new FilesContainer(new FilesRegion("ORD", null, null), "test.cyberduck.ch")));
        assertTrue(list.contains(new FilesContainer(new FilesRegion("DFW", null, null), "test.cyberduck.ch")));
    }
}
