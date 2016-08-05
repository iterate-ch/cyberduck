package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PreferencesUseragentProviderTest {

    @Test
    public void testGet() throws Exception {
        assertTrue(new PreferencesUseragentProvider().get().startsWith("Cyberduck/"));
    }
}
