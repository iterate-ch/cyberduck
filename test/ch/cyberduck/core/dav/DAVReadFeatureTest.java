package ch.cyberduck.core.dav;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

/**
 * @version $Id$
 */
public class DAVReadFeatureTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final TransferStatus status = new TransferStatus();
        new DAVReadFeature(session).read(new Path(session.workdir(), "nosuchname", Path.FILE_TYPE), status);
    }


}
