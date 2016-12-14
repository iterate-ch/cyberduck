package ch.cyberduck.core.azure;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class AzureUrlProviderTest {

    @Test
    public void testGet() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                System.getProperties().getProperty("azure.account"), System.getProperties().getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()).connect(session, PathCache.empty());
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, "f g", EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(test, new TransferStatus());
        assertNotNull("https://kahy9boj3eib.blob.core.windows.net/cyberduck/f%20g?sp=r&sr=b&sv=2012-02-12&se=2014-01-29T14%3A48%3A26Z&sig=HlAF9RjXNic2%2BJa2ghOgs8MTgJva4bZqNZrb7BIv2mI%3D",
                new AzureUrlProvider(session).toUrl(test).find(DescriptiveUrl.Type.signed).getUrl());
        new AzureDeleteFeature(session, null).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testDisconnected() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                System.getProperties().getProperty("azure.account"), System.getProperties().getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        final UrlProvider provider = new AzureUrlProvider(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        assertEquals(DescriptiveUrl.EMPTY, provider.toUrl(file).find(DescriptiveUrl.Type.signed));
    }
}
