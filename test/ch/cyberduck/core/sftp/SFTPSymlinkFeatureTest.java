package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class SFTPSymlinkFeatureTest extends AbstractTestCase {

    @Test
    public void testSymlink() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path target = new Path(session.home(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        new SFTPTouchFeature(session).touch(target);
        final Path link = new Path(session.home(), UUID.randomUUID().toString(), Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE);
        new SFTPSymlinkFeature(session).symlink(link, target.getName());
        assertTrue(new SFTPFindFeature(session).find(link));
        assertEquals(Path.FILE_TYPE | Path.SYMBOLIC_LINK_TYPE,
                session.list(session.home(), new DisabledListProgressListener()).get(link.getReference()).attributes().getType());
        new SFTPDeleteFeature(session).delete(Collections.singletonList(link), new DisabledLoginController());
        assertFalse(new SFTPFindFeature(session).find(link));
        assertTrue(new SFTPFindFeature(session).find(target));
        new SFTPDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginController());
        session.close();
    }
}
