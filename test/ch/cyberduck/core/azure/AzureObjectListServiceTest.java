package ch.cyberduck.core.azure;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class AzureObjectListServiceTest extends AbstractTestCase {

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.volume));
        final AttributedList<Path> list = new AzureObjectListService(session).list(container, new DisabledListProgressListener());
        assertNotNull(list);
    }
}
