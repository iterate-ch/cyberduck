package ch.cyberduck.core;

import org.junit.Assert;
import org.junit.Test;

public class IOKitSleepPreventerTest {

    @Test
    public void testRelease() throws Exception {
        final SleepPreventer s = new IOKitSleepPreventer();
        final String lock = s.lock();
        Assert.assertNotNull(lock);
        s.release(lock);
    }
}
