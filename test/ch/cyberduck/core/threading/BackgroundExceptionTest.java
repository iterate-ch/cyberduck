package ch.cyberduck.core.threading;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;

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
        assertEquals("Error", e.getTitle());
    }

    @Test
    public void testGetMessageIO() throws Exception {
        final BackgroundException e = new BackgroundException(new IOException());
        assertEquals("Unknown", e.getMessage());
        assertEquals("I/O Error", e.getTitle());
    }

    @Test
    public void testIOMessage() throws Exception {
        final BackgroundException e = new BackgroundException(new SocketException("s"));
        assertEquals("Unknown", e.getMessage());
        assertEquals("s", e.getDetail());
        assertEquals("Network Error", e.getTitle());
    }
}
