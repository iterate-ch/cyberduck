package ch.cyberduck.core.exception;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.io.IOException;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @version $Id$
 */
public class BackgroundExceptionTest extends AbstractTestCase {

    @Test
    public void testGetMessage() throws Exception {
        final BackgroundException e = new BackgroundException(new LoginCanceledException());
        e.setMessage("m");
        assertEquals("m", e.getMessage());
    }

    @Test
    public void testGetMessageIO() throws Exception {
        final BackgroundException e = new BackgroundException(new IOException("m"));
        assertNull(e.getMessage());
        assertEquals("m.", e.getDetail());
    }

    @Test
    public void testIOMessage() throws Exception {
        final BackgroundException e = new BackgroundException(new SocketException("s"));
        e.setMessage("m.");
        assertEquals("s.", e.getDetail());
    }
}
