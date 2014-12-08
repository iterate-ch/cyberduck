package ch.cyberduck.core.i18n;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class BundleLocaleTest extends AbstractTestCase {

    @Test
    public void testGet() throws Exception {
        assertEquals("Il y a eu un problème lors de la recherche de mises à jour",
                new BundleLocale().localize("Il y a eu un problème lors de la recherche de mises à jour", "Localizable"));
    }
}


