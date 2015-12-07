package ch.cyberduck.core.urlhandler;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class SchemeHandlerFactoryTest extends AbstractTestCase {

    @Test
    public void testGet() throws Exception {
        assertNotNull(SchemeHandlerFactory.get());
    }
}
