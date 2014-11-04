package ch.cyberduck.core.serializer.impl;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.features.Location;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

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
        for(Local l : new Local("profiles").list().filter(new Filter<Local>() {
            @Override
            public boolean accept(final Local file) {
                return file.getName().endsWith(".cyberduckprofile");
            }
        })) {
            final Profile profile = new ProfilePlistReader().read(l);
            assertNotNull(profile);
        }
    }

    @Test
    public void testRegions() throws Exception {
        final Profile profile = new ProfilePlistReader().read(
                LocalFactory.get("test/ch/cyberduck/core/serializer/impl/Custom Regions S3.cyberduckprofile")
        );
        assertNotNull(profile);
        assertEquals(2, profile.getRegions().size());
        final Iterator<Location.Name> iter = profile.getRegions().iterator();
        assertEquals("custom", iter.next().getIdentifier());
        assertEquals("custom2", iter.next().getIdentifier());
    }
}
