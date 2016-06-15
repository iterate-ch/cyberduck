package ch.cyberduck.core;

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ResolveCanceledException;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ResolveCanceledExceptionTest {

    @Test
    public void testInstance() {
        assertTrue(new ResolveCanceledException() instanceof ConnectionCanceledException);
    }
}
