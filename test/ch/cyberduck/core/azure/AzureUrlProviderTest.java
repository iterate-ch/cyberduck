package ch.cyberduck.core.azure;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
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
public class AzureUrlProviderTest extends AbstractTestCase {

    @Test
    public void testGet() throws Exception {
        final Host host = new Host(new AzureProtocol(), "cyberduck.blob.core.windows.net", new Credentials(
                properties.getProperty("azure.account"), properties.getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, Cache.<Path>empty());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.volume));
        assertNotNull("https://cyberduck.blob.core.windows.net/cyberduck/f%20g?sp=r&sr=b&sv=2012-02-12&se=2014-01-29T14%3A48%3A26Z&sig=HlAF9RjXNic2%2BJa2ghOgs8MTgJva4bZqNZrb7BIv2mI%3D",
                new AzureUrlProvider(session).toUrl(new Path(container, "f g", EnumSet.of(Path.Type.file))).find(DescriptiveUrl.Type.signed).getUrl());
        session.close();
    }
}
