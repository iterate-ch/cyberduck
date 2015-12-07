package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SwiftTouchFeatureTest extends AbstractTestCase {

    @Test
    public void testFile() {
        final SwiftSession session = new SwiftSession(new Host(new SwiftProtocol(), "h"));
        assertFalse(new SwiftTouchFeature(session).isSupported(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertTrue(new SwiftTouchFeature(session).isSupported(new Path("/container", EnumSet.of(Path.Type.directory, Path.Type.volume))));
    }
}
