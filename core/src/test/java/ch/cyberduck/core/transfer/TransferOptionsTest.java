package ch.cyberduck.core.transfer;

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransferOptionsTest {

    @Test
    public void testQuarantine() {
        assertEquals(PreferencesFactory.get().getBoolean("queue.download.quarantine"), new TransferOptions().quarantine);
    }
}
