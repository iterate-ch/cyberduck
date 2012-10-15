package ch.cyberduck.core.serializer.impl;

import ch.cyberduck.core.Profile;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.ProtocolReaderFactory;

import org.junit.BeforeClass;

import junit.framework.TestCase;

/**
 * @version $Id:$
 */
public class ProtocolPlistReaderTest extends TestCase {

    @BeforeClass
    public static void register() {
        ProtocolPlistReader.register();
    }

    public void testDeserialize() throws Exception {
        final Profile profile = ProtocolReaderFactory.get().read(
                LocalFactory.createLocal("test/ch/cyberduck/core/serializer/impl/Dropbox.cyberduckprofile")
        );
        assertFalse(profile.isEnabled());
    }
}
