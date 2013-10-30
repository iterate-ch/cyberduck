package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id:$
 */
public class SFTPFindFeatureTest extends AbstractTestCase {

    @Test
    public void testFind() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertFalse(new SFTPFindFeature(session).find(new Path(UUID.randomUUID().toString(), Path.FILE_TYPE)));
    }
}
