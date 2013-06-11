package ch.cyberduck.core.exception;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

import java.io.IOException;

/**
 * @version $Id$
 */
public class HttpResponseExceptionMappingService extends AbstractIOExceptionMappingService<HttpResponseException> {

    @Override
    public IOException map(final String help, final HttpResponseException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, help);
        this.append(buffer, String.valueOf(e.getStatusCode()));
        this.append(buffer, e.getMessage());
        if(e.getStatusCode() == HttpStatus.SC_FORBIDDEN
                || e.getStatusCode() == HttpStatus.SC_UNAUTHORIZED || e.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
            return new LoginFailureException(buffer.toString(), e);
        }
        return new IOException(buffer.toString(), e);
    }
}
