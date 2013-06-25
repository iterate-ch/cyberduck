package ch.cyberduck.core.serializer.impl;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.ProfileReaderFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class ProfilePlistReaderTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        ProfilePlistReader.register();
    }

    @Test
    public void testDeserialize() throws Exception {
        final Profile profile = ProfileReaderFactory.get().read(
                LocalFactory.createLocal("test/ch/cyberduck/core/serializer/impl/Dropbox.cyberduckprofile")
        );
        assertFalse(profile.isEnabled());
    }

    @Test
    public void testAll() throws Exception {
        for(Local l : LocalFactory.createLocal("profiles").list().filter(null, new Filter<Local>() {
            @Override
            public boolean accept(final Local file) {
                return file.getName().endsWith(".cyberduckprofile");
            }
        })) {
            final Profile profile = ProfileReaderFactory.get().read(l);
            assertNotNull(profile);
        }
    }
}
