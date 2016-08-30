package ch.cyberduck.core.i18n;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BundleLocaleTest {

    @Test
    public void testGet() throws Exception {
        assertEquals("Il y a eu un problème lors de la recherche de mises à jour",
                new BundleLocale().localize("Il y a eu un problème lors de la recherche de mises à jour", "Localizable"));
    }
}
