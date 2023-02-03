package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class AzureUrlProviderTest extends AbstractAzureTest {

    @Test
    public void testGet() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, "f g", EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(test, new TransferStatus());
        assertNotNull("https://kahy9boj3eib.blob.core.windows.net/cyberduck/f%20g?sp=r&sr=b&sv=2012-02-12&se=2014-01-29T14%3A48%3A26Z&sig=HlAF9RjXNic2%2BJa2ghOgs8MTgJva4bZqNZrb7BIv2mI%3D",
            new AzureUrlProvider(session).toDownloadUrl(test, null, new DisabledPasswordCallback()).getUrl());
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDisconnected() throws Exception {
        final Host host = new Host(new AzureProtocol(), "kahy9boj3eib.blob.core.windows.net", new Credentials(
                System.getProperties().getProperty("azure.user"), System.getProperties().getProperty("azure.key")
        ));
        final AzureSession session = new AzureSession(host);
        final AzureUrlProvider provider = new AzureUrlProvider(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertEquals(DescriptiveUrl.EMPTY, provider.toDownloadUrl(file, null, new DisabledPasswordCallback()));
    }
}
