package ch.cyberduck.core;

import ch.cyberduck.ui.cocoa.UserDefaultsPreferences;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class UserDefaultsPreferencesTest extends AbstractTestCase {

    @Test
    public void testGetListEscapedWhitespace() throws Exception {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("metadata",
                "Cache-Control=public,max-age=31536000 Expires=Fri,\\ 01\\ Feb\\ 2013\\ 00:00:00\\ GMT");

        final List<String> properties = p.getList("metadata");
        assertTrue(properties.contains("Cache-Control=public,max-age=31536000"));
        assertTrue(properties.contains("Expires=Fri,\\ 01\\ Feb\\ 2013\\ 00:00:00\\ GMT"));
    }

    @Test
    public void testGetList() throws Exception {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("metadata",
                "a b");

        final List<String> properties = p.getList("metadata");
        assertTrue(properties.contains("a"));
        assertTrue(properties.contains("b"));
    }

    @Test
    public void testLong() {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("test.p", 2L);
        assertEquals("2", p.getProperty("test.p"));
    }

    @Test
    public void testDouble() {
        Preferences p = new UserDefaultsPreferences();
        p.load();
        p.setProperty("test.p", 0.983652);
        assertEquals("0.983652", p.getProperty("test.p"));
    }
}