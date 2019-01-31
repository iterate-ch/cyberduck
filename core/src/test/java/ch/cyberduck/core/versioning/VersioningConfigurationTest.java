package ch.cyberduck.core.versioning;

import ch.cyberduck.core.VersioningConfiguration;

import org.junit.Test;

import static org.junit.Assert.*;

public class VersioningConfigurationTest {

    @Test
    public void testEquals() {
        assertEquals(VersioningConfiguration.empty(), new VersioningConfiguration());
        assertEquals(new VersioningConfiguration(true), new VersioningConfiguration(true));
        assertEquals(new VersioningConfiguration(false), new VersioningConfiguration(false));
        assertNotEquals(new VersioningConfiguration(true), new VersioningConfiguration(false));
        assertNotEquals(new VersioningConfiguration(true), new VersioningConfiguration(true, true));
    }
}
