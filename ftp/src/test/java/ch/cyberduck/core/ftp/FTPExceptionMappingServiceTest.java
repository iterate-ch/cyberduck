package ch.cyberduck.core.ftp;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;

import org.junit.Test;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FTPExceptionMappingServiceTest {

    @Test
    public void testMap() {
        assertEquals(ConnectionRefusedException.class,
            new FTPExceptionMappingService().map(new SocketException("Software caused connection abort")).getClass());
        assertEquals(ConnectionRefusedException.class,
            new FTPExceptionMappingService().map(new SocketException("Socket closed")).getClass());
    }

    @Test
    public void testQuota() {
        assertTrue(new FTPExceptionMappingService().map(new FTPException(452, "")) instanceof QuotaException);
    }

    @Test
    public void testLogin() {
        assertTrue(new FTPExceptionMappingService().map(new FTPException(530, "")) instanceof LoginFailureException);
    }

    @Test
    public void testServiceNotAvailable() {
        final BackgroundException failure = new FTPExceptionMappingService().map(new FTPException(421, "Too many connections (25) from this IP"));
        assertTrue(failure instanceof RetriableAccessDeniedException);
        assertEquals("Too many connections (25) from this IP. Please contact your web hosting service provider for assistance.", failure.getDetail());
    }

    @Test
    public void testFile() {
        assertTrue(new FTPExceptionMappingService().map(new FTPException(550, "")) instanceof NotfoundException);
    }

    @Test
    public void testTrim() {
        assertEquals("M. Please contact your web hosting service provider for assistance.", new FTPExceptionMappingService().map(new FTPException(500, "m\n")).getDetail());
    }

    @Test
    public void testSocketTimeout() {
        assertEquals(ConnectionTimeoutException.class, new FTPExceptionMappingService()
            .map(new SocketTimeoutException()).getClass());
        assertEquals(ConnectionTimeoutException.class, new FTPExceptionMappingService()
            .map("message", new SocketTimeoutException()).getClass());
        assertEquals(ConnectionTimeoutException.class, new FTPExceptionMappingService()
            .map("message", new SocketTimeoutException(), new Path("/f", EnumSet.of(Path.Type.file))).getClass());
    }
}
