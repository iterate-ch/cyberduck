package ch.cyberduck.core.azure;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class AzureLoggingFeatureTest extends AbstractAzureTest {

    @Test
    public void testSetConfiguration() throws Exception {
        final Path container = new Path("/cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final AzureLoggingFeature feature = new AzureLoggingFeature(session, null);
        feature.setConfiguration(container, new LoggingConfiguration(false));
        assertFalse(feature.getConfiguration(container).isEnabled());
        feature.setConfiguration(container, new LoggingConfiguration(true));
        assertTrue(feature.getConfiguration(container).isEnabled());
    }
}
