package ch.cyberduck.core.exception;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.io.IOException;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class BackgroundExceptionTest extends AbstractTestCase {

    @Test
    public void testGetMessage() throws Exception {
        final BackgroundException e = new BackgroundException(new LoginCanceledException());
        assertEquals("Unknown", e.getMessage());
    }

    @Test
    public void testGetMessageIO() throws Exception {
        final BackgroundException e = new BackgroundException(new IOException());
        assertEquals("Unknown", e.getMessage());
    }

    @Test
    public void testIOMessage() throws Exception {
        final BackgroundException e = new BackgroundException(new SocketException("s"));
        assertEquals("Unknown", e.getMessage());
        assertEquals("s", e.getDetail());
        assertEquals("Unknown. s", e.toString());
    }
}
