package ch.cyberduck.core.ftp;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.ftp.FTPException;
import ch.cyberduck.core.ftp.FTPExceptionMappingService;

import org.junit.Assert;
import org.junit.Test;

import java.net.SocketException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class FTPExceptionMappingServiceTest extends AbstractTestCase {

    @Test
    public void testMap() throws Exception {
        Assert.assertEquals(ConnectionCanceledException.class,
                new DefaultIOExceptionMappingService().map(new SocketException("Software caused connection abort")).getClass());
        assertEquals(ConnectionCanceledException.class,
                new DefaultIOExceptionMappingService().map(new SocketException("Socket closed")).getClass());
    }

    @Test
    public void testQuota() throws Exception {
        assertTrue(new FTPExceptionMappingService().map(new FTPException(452, "")) instanceof QuotaException);
    }

    @Test
    public void testLogin() throws Exception {
        assertTrue(new FTPExceptionMappingService().map(new FTPException(530, "")) instanceof LoginFailureException);
    }

    @Test
    public void testFile() throws Exception {
        assertTrue(new FTPExceptionMappingService().map(new FTPException(550, "")) instanceof NotfoundException);
    }

    @Test
    public void testTrim() throws Exception {
        assertEquals("m.", new FTPExceptionMappingService().map(new FTPException(500, "m\n")).getDetail());
    }
}
