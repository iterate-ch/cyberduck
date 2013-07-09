package ch.cyberduck.core.ftp;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id:$
 */
public class FTPParserSelectorTest extends AbstractTestCase {

    @Test
    public void testGetParser() throws Exception {
        assertNotNull(new FTPParserSelector().getParser(null));
    }
}
