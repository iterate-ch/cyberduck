package ch.cyberduck.core.i18n;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocaleFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import java.text.MessageFormat;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class BundleLocaleTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        BundleLocale.register();
    }

    @Test
    public void testGet() throws Exception {
        assertEquals("La clé d''hôte fournie est {0}.",
                LocaleFactory.localizedString("La clé d'hôte fournie est {0}.", "Localizable"));
        assertEquals("Il y a eu un problème lors de la recherche de mises à jour",
                LocaleFactory.localizedString("Il y a eu un problème lors de la recherche de mises à jour", "Localizable"));
    }

    @Test
    public void testFormat() throws Exception {
        assertEquals("La clé d'hôte fournie est 1.",
                MessageFormat.format(LocaleFactory.localizedString("La clé d'hôte fournie est {0}."), "1"));
    }
}


