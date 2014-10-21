package ch.cyberduck.core.dav;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class DAVAttributesFeatureTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path(UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        final DAVAttributesFeature f = new DAVAttributesFeature(session);
        f.find(test);
    }

    @Test
    public void testFind() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path("/trunk/LICENSE.txt", EnumSet.of(Path.Type.file));
        final DAVAttributesFeature f = new DAVAttributesFeature(session);
        final PathAttributes attributes = f.find(test);
        assertEquals(923, attributes.getSize());
        assertNotNull(attributes.getModificationDate());
        assertNotNull(attributes.getETag());
    }
}
