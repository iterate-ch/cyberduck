package ch.cyberduck.core.exception;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.io.IOException;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class SFTPExceptionMappingServiceTest extends AbstractTestCase {

    @Test
    public void testMapReadFailure() throws Exception {
        assertEquals(SocketException.class, new SFTPExceptionMappingService().map(new IOException("Unexpected end of sftp stream.")).getCause().getClass());
    }
}
