package ch.cyberduck.core.lifecycle;

import org.junit.Test;

import static org.junit.Assert.*;

public class LifecycleConfigurationTest {

    @Test
    public void testEquals() {
        assertEquals(LifecycleConfiguration.empty(), new LifecycleConfiguration());
        assertEquals(new LifecycleConfiguration(1, "GLACIER", 1), new LifecycleConfiguration(1, "GLACIER", 1));
        assertEquals(new LifecycleConfiguration(1, "GLACIER", 2), new LifecycleConfiguration(1, "GLACIER", 2));
        assertNotEquals(new LifecycleConfiguration(1, "GLACIER", 2), new LifecycleConfiguration(2, "GLACIER", 1));
    }
}
