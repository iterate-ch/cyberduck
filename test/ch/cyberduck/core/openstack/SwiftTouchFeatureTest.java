package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class SwiftTouchFeatureTest extends AbstractTestCase {

    @Test
    public void testFile() {
        final SwiftSession session = new SwiftSession(new Host(Protocol.SWIFT, "h"));
        assertFalse(new SwiftTouchFeature(session).isSupported(new Path("/", Path.VOLUME_TYPE)));
        assertTrue(new SwiftTouchFeature(session).isSupported(new Path("/container", Path.VOLUME_TYPE)));
    }
}
