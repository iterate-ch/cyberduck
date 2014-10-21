package ch.cyberduck.core.serializer.impl;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Profile;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @version $Id$
 */
public class ProfilePlistReaderTest extends AbstractTestCase {

    @Test
    public void testDeserialize() throws Exception {
        final Profile profile = new ProfilePlistReader().read(
                LocalFactory.get("test/ch/cyberduck/core/serializer/impl/Dropbox.cyberduckprofile")
        );
        assertNull(profile);
    }

    @Test
    public void testAll() throws Exception {
        for(Local l : LocalFactory.get("profiles").list().filter(new Filter<Local>() {
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
