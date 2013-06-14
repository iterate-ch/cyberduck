package ch.cyberduck.core.cdn;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @version $Id:$
 */
public class DistributionTest extends AbstractTestCase {

    @Test
    public void testEquals() throws Exception {
        assertEquals(new Distribution("o", Distribution.DOWNLOAD), new Distribution("o", Distribution.DOWNLOAD));
        assertFalse(new Distribution("a", Distribution.DOWNLOAD).equals(new Distribution("o", Distribution.DOWNLOAD)));
        assertFalse(new Distribution("o", Distribution.DOWNLOAD).equals(new Distribution("o", Distribution.STREAMING)));
    }
}
