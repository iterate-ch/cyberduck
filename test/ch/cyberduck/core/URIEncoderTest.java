package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class URIEncoderTest extends AbstractTestCase {

    @Test
    public void testEncode() throws Exception {
        assertEquals("/p", URIEncoder.encode("/p"));
        assertEquals("/p%20d", URIEncoder.encode("/p d"));
    }
}
