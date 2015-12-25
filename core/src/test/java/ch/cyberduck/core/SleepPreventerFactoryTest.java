package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * @version $Id:$
 */
public class SleepPreventerFactoryTest {

    @Test
    public void testLock() throws Exception {
        assertNull(SleepPreventerFactory.get().lock());
    }
}
