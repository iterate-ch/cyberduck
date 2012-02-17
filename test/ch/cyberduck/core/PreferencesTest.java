package ch.cyberduck.core;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class PreferencesTest extends AbstractTestCase {

    @Test
    public void testGetList() throws Exception {
        Preferences p = Preferences.instance();
        p.setProperty("metadata",
                "Cache-Control=public,max-age=31536000 Expires=Fri,\\ 01\\ Feb\\ 2013\\ 00:00:00\\ GMT");

        final List<String> properties = p.getList("metadata");
        assertTrue(properties.contains("Cache-Control=public,max-age=31536000"));
        assertTrue(properties.contains("Expires=Fri,\\ 01\\ Feb\\ 2013\\ 00:00:00\\ GMT"));
    }
}