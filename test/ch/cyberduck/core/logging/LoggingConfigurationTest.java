package ch.cyberduck.core.logging;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class LoggingConfigurationTest extends AbstractTestCase {

    @Test
    public void testEquals() throws Exception {
        assertEquals(LoggingConfiguration.empty(), new LoggingConfiguration());
        assertEquals(new LoggingConfiguration(true), new LoggingConfiguration(true));
        assertEquals(new LoggingConfiguration(false), new LoggingConfiguration(false));
        assertFalse(new LoggingConfiguration(true).equals(new LoggingConfiguration(false)));
        assertFalse(new LoggingConfiguration(true).equals(new LoggingConfiguration(true, "a")));
        assertFalse(new LoggingConfiguration(true, "b").equals(new LoggingConfiguration(true, "a")));
    }
}
