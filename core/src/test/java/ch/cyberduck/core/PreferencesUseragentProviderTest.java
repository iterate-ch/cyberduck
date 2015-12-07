package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class PreferencesUseragentProviderTest extends AbstractTestCase {

    @Test
    public void testGet() throws Exception {
        assertTrue(new PreferencesUseragentProvider().get().startsWith("Cyberduck/"));
    }
}
