package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PreferencesUseragentProviderTest {

    @Test
    public void testGet() {
        assertTrue(new PreferencesUseragentProvider().get().startsWith("Cyberduck/"));
    }
}
