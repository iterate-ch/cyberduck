package ch.cyberduck.core.transfer;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.download.DownloadFilterOptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DownloadFilterOptionsTest {

    @Test
    public void testQuarantine() {
        assertEquals(PreferencesFactory.get().getBoolean("queue.download.quarantine"), new DownloadFilterOptions(new Host(new TestProtocol())).quarantine);
    }
}
