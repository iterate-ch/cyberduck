package ch.cyberduck.core.text;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class NaturalOrderComparatorTest extends AbstractTestCase {

    @Test
    public void testCompare() throws Exception {
        assertEquals(-1, new NaturalOrderComparator().compare("123a", "a"));
        assertEquals(-1, new NaturalOrderComparator().compare("365", "400"));
    }
}
