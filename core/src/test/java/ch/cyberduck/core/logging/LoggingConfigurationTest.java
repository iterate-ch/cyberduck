package ch.cyberduck.core.logging;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoggingConfigurationTest {

    @Test
    public void testEquals() {
        assertEquals(LoggingConfiguration.empty(), new LoggingConfiguration());
        assertEquals(new LoggingConfiguration(true), new LoggingConfiguration(true));
        assertEquals(new LoggingConfiguration(false), new LoggingConfiguration(false));
        assertNotEquals(new LoggingConfiguration(true), new LoggingConfiguration(false));
        assertNotEquals(new LoggingConfiguration(true), new LoggingConfiguration(true, "a"));
        assertNotEquals(new LoggingConfiguration(true, "b"), new LoggingConfiguration(true, "a"));
    }
}
