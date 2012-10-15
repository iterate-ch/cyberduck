package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullKeychain;
import ch.cyberduck.core.editor.LaunchServicesApplicationFinder;
import ch.cyberduck.core.local.LocalFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class FilezillaBookmarkCollectionTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        LaunchServicesApplicationFinder.register();
        NullKeychain.register();
    }

    @Test
    public void testParse() {
        FilezillaBookmarkCollection c = new FilezillaBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(LocalFactory.createLocal("test/ch/cyberduck/core/importer/org.filezilla-project.sitemanager.xml"));
        assertEquals(2, c.size());
    }
}