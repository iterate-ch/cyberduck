package ch.cyberduck.core.azure;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
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
import static org.junit.Assert.assertFalse;

@Category(IntegrationTest.class)
public class AzureUrlProviderTest extends AbstractAzureTest {

    @Test
    public void testGet() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(test, new TransferStatus());
        final DescriptiveUrlBag urls = new AzureUrlProvider(session, new DisabledPasswordStore() {
            @Override
            public String findLoginPassword(final Host bookmark) {
                return PROPERTIES.get("azure.password");
            }
        }).toUrl(test).filter(DescriptiveUrl.Type.signed);
        assertEquals(5, urls.size());
        for(DescriptiveUrl url : urls) {
            assertFalse(url.getUrl().isEmpty());
        }
        new AzureDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
