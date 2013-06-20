package ch.cyberduck.core.exception;

import ch.cyberduck.core.AbstractTestCase;

import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import com.rackspacecloud.client.cloudfiles.FilesException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class FilesExceptionMappingServiceTest extends AbstractTestCase {

    @Test
    public void testLoginFailure() throws Exception {
        final FilesException f = new FilesException(
                "message", new Header[]{}, new BasicStatusLine(new ProtocolVersion("http", 1, 1), 403, "Forbidden"));
        assertTrue(new FilesExceptionMappingService().map(f) instanceof LoginFailureException);
        assertEquals("Login failed", new FilesExceptionMappingService().map(f).getMessage());
        assertEquals("message. 403 Forbidden.", new FilesExceptionMappingService().map(f).getDetail());
    }

    @Test
    public void testMap() throws Exception {
        assertEquals("message. 500 reason.", new FilesExceptionMappingService().map(
                new FilesException("message", null, new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public int getStatusCode() {
                        return 500;
                    }

                    @Override
                    public String getReasonPhrase() {
                        return "reason";
                    }
                })).getDetail());
    }
}
