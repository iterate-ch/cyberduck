package ch.cyberduck.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * @version $Id:$
 */
public class CollectionTest {

    @Test
    public void testClear() throws Exception {
        Collection<Object> c = new Collection<Object>();
        c.add(new Object());
        c.clear();
        Assert.assertTrue(c.isEmpty());
    }
}
