package ch.cyberduck.core.transfer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.UserDefaultsPreferences;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @version $Id$
 */
public class TransferOptionsTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        UserDefaultsPreferences.register();
    }

    @Test
    public void testQuarantine() {
        Assert.assertEquals(Preferences.instance().getBoolean("queue.download.quarantine"), new TransferOptions().quarantine);
    }
}
