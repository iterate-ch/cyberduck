package ch.cyberduck.core;

import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class IOKitSleepPreventerTest extends AbstractTestCase {

    @Test
    public void testRelease() throws Exception {
        final SleepPreventer s = new IOKitSleepPreventer();
        final String lock = s.lock();
        assertNotNull(lock);
        s.release(lock);
    }
}
