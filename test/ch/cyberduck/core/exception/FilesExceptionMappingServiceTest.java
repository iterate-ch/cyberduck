package ch.cyberduck.core.exception;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.junit.Test;

import com.rackspacecloud.client.cloudfiles.FilesException;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class FilesExceptionMappingServiceTest {

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
                })).getMessage());
    }
}
