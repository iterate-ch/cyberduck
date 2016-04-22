package ch.cyberduck.core.ftp;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;

import org.junit.Test;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FTPExceptionMappingServiceTest {

    @Test
    public void testMap() throws Exception {
        assertEquals(ConnectionCanceledException.class,
                new FTPExceptionMappingService().map(new SocketException("Software caused connection abort")).getClass());
        assertEquals(ConnectionCanceledException.class,
                new FTPExceptionMappingService().map(new SocketException("Socket closed")).getClass());
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
        assertEquals("M. Please contact your web hosting service provider for assistance.", new FTPExceptionMappingService().map(new FTPException(500, "m\n")).getDetail());
    }

    @Test
    public void testSocketTimeout() throws Exception {
        assertEquals(ConnectionTimeoutException.class, new FTPExceptionMappingService()
                .map(new SocketTimeoutException()).getClass());
        assertEquals(ConnectionTimeoutException.class, new FTPExceptionMappingService()
                .map("message", new SocketTimeoutException()).getClass());
        assertEquals(ConnectionTimeoutException.class, new FTPExceptionMappingService()
                .map("message", new SocketTimeoutException(), new Path("/f", EnumSet.of(Path.Type.file))).getClass());
    }
}
