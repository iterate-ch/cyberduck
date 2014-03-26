package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class DefaultLocalTouchFeatureTest extends AbstractTestCase {

    @Test
    public void testTouch() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final DefaultLocalTouchFeature f = new DefaultLocalTouchFeature();
        f.touch(l);
        assertTrue(l.exists());
        f.touch(l);
        assertTrue(l.exists());
        l.delete();
    }

}
