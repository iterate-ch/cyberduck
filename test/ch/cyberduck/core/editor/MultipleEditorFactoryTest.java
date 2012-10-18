package ch.cyberduck.core.editor;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @version $Id$
 */
public class MultipleEditorFactoryTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        LaunchServicesApplicationFinder.register();
        MultipleEditorFactory.register();
    }

    @Test(expected = FactoryException.class)
    public void testEdit() {
        MultipleEditorFactory f = new MultipleEditorFactory();
        f.create();
    }
}
