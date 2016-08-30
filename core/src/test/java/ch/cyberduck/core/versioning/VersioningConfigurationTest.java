package ch.cyberduck.core.versioning;

import ch.cyberduck.core.VersioningConfiguration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class VersioningConfigurationTest {

    @Test
    public void testEquals() throws Exception {
        assertEquals(VersioningConfiguration.empty(), new VersioningConfiguration());
        assertEquals(new VersioningConfiguration(true), new VersioningConfiguration(true));
        assertEquals(new VersioningConfiguration(false), new VersioningConfiguration(false));
        assertFalse(new VersioningConfiguration(true).equals(new VersioningConfiguration(false)));
        assertFalse(new VersioningConfiguration(true).equals(new VersioningConfiguration(true, true)));
    }
}
