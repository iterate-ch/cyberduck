package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DescriptiveUrlTest extends AbstractTestCase {

    @Test
    public void testEquals() throws Exception {
        assertTrue(new DescriptiveUrl("http://host.domain", "a").equals(new DescriptiveUrl("http://host.domain", "b")));
        assertFalse(new DescriptiveUrl("http://host.domainb", "a").equals(new DescriptiveUrl("http://host.domain", "b")));
    }

    @Test
    public void testDefault() throws Exception {
        assertEquals("Open in Web Browser", new DescriptiveUrl("http://me").getHelp());
    }
}
