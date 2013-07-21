package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class LocalTest extends AbstractTestCase {

    @Test
    public void testList() throws Exception {
        assertFalse(new Local("profiles") {

        }.list().isEmpty());
    }
}
