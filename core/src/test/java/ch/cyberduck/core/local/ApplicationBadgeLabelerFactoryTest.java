package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id:$
 */
public class ApplicationBadgeLabelerFactoryTest extends AbstractTestCase {

    @Test
    public void testGet() throws Exception {
        assertNotNull(ApplicationBadgeLabelerFactory.get());
    }
}
