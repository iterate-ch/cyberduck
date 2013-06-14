package ch.cyberduck.core.threading;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LoginCanceledException;

import org.junit.Test;

import java.net.SocketException;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class BackgroundExceptionTest extends AbstractTestCase {

    @Test
    public void testGetMessage() throws Exception {
        final BackgroundException e = new BackgroundException(new LoginCanceledException());
        assertEquals("Unknown", e.getMessage());
        assertEquals("I/O Error", e.getReadableTitle());
    }

    @Test
    public void testIOMessage() throws Exception {
        final BackgroundException e = new BackgroundException(new SocketException("s"));
        assertEquals("Unknown", e.getMessage());
        assertEquals("s", e.getDetailedCauseMessage());
        assertEquals("Network Error", e.getReadableTitle());
    }
}
