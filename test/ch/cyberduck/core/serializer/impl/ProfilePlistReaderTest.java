package ch.cyberduck.core.serializer.impl;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Profile;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class ProfilePlistReaderTest extends AbstractTestCase {

    @Test
    public void testDeserialize() throws Exception {
        final Profile profile = new ProfilePlistReader().read(
                LocalFactory.createLocal("test/ch/cyberduck/core/serializer/impl/Dropbox.cyberduckprofile")
        );
        assertFalse(profile.isEnabled());
    }

    @Test
    public void testAll() throws Exception {
        for(Local l : LocalFactory.createLocal("profiles").list().filter(new Filter<Local>() {
            @Override
            public boolean accept(final Local file) {
                return file.getName().endsWith(".cyberduckprofile");
            }
        })) {
            final Profile profile = new ProfilePlistReader().read(l);
            assertNotNull(profile);
        }
    }
}
