package ch.cyberduck.core.local;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id:$
 */
public class ApplicationBadgeLabelerFactoryTest {

    @Test
    public void testGet() throws Exception {
        assertNotNull(ApplicationBadgeLabelerFactory.get());
    }
}
