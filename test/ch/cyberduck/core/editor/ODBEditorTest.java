package ch.cyberduck.core.editor;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @version $Id:$
 */
public class ODBEditorTest extends AbstractTestCase {

    @Before
    @Override
    public void register() {
        super.register();
        LaunchServicesApplicationFinder.register();
        ODBEditorFactory.register();
    }

    @Test
    @Ignore
    public void testEdit() throws Exception {

    }
}
