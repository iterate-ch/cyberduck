package ch.cyberduck.core;

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ResolveCanceledException;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class ResolveCanceledExceptionTest extends AbstractTestCase {

    @Test
    public void testInstance() {
        assertTrue(new ResolveCanceledException() instanceof ConnectionCanceledException);
    }
}
