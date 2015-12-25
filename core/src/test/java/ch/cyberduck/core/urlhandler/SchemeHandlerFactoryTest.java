package ch.cyberduck.core.urlhandler;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class SchemeHandlerFactoryTest {

    @Test
    public void testGet() throws Exception {
        assertNotNull(SchemeHandlerFactory.get());
    }
}
