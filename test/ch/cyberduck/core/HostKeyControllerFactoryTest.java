package ch.cyberduck.core;

import org.junit.Test;

/**
 * @version $Id:$
 */
public class HostKeyControllerFactoryTest extends AbstractTestCase {

    @Test(expected = FactoryException.class)
    public void testGet() throws Exception {
        HostKeyControllerFactory.get(null);
    }
}
