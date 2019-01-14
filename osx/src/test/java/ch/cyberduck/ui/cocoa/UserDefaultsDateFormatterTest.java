package ch.cyberduck.ui.cocoa;

import ch.cyberduck.core.date.UserDateFormatter;

import org.junit.Test;

import java.util.TimeZone;

import static org.junit.Assert.assertNotNull;

public class UserDefaultsDateFormatterTest {

    @Test
    public void testGetShortFormat() throws Exception {
        final UserDateFormatter f = new UserDefaultsDateFormatter(TimeZone.getDefault().getID());
        assertNotNull(f.getShortFormat(System.currentTimeMillis(), false));
        assertNotNull(f.getShortFormat(System.currentTimeMillis(), true));
    }

    @Test
    public void testGetMediumFormat() throws Exception {
        final UserDateFormatter f = new UserDefaultsDateFormatter(TimeZone.getDefault().getID());
        assertNotNull(f.getMediumFormat(System.currentTimeMillis(), false));
        assertNotNull(f.getMediumFormat(System.currentTimeMillis(), true));
    }

    @Test
    public void testGetLongFormat() throws Exception {
        final UserDateFormatter f = new UserDefaultsDateFormatter(TimeZone.getDefault().getID());
        assertNotNull(f.getLongFormat(System.currentTimeMillis(), false));
        assertNotNull(f.getLongFormat(System.currentTimeMillis(), true));
    }
}
