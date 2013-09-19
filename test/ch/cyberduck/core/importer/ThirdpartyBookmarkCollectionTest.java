package ch.cyberduck.core.importer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.local.FinderLocal;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class ThirdpartyBookmarkCollectionTest extends AbstractTestCase {

    @Test
    public void testLoad() throws Exception {
        final FinderLocal source = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        source.touch();
        IOUtils.write(RandomStringUtils.random(1000), source.getOutputStream(false));
        final AtomicBoolean r = new AtomicBoolean();
        final ThirdpartyBookmarkCollection c = new ThirdpartyBookmarkCollection() {
            @Override
            public Local getFile() {
                return source;
            }

            @Override
            protected void parse(final Local file) {
                r.set(true);
            }

            @Override
            public String getBundleIdentifier() {
                return "t";
            }
        };
        c.load();
        assertTrue(r.get());
        r.set(false);
        Preferences.instance().setProperty(c.getConfiguration(), true);
        c.load();
        assertFalse(r.get());
    }
}
