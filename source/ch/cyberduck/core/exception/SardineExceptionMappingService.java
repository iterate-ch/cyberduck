package ch.cyberduck.core.exception;

import org.apache.http.HttpStatus;

import java.io.IOException;

import com.googlecode.sardine.impl.SardineException;

/**
 * @version $Id:$
 */
public class SardineExceptionMappingService extends AbstractIOExceptionMappingService<SardineException> {

    @Override
    public IOException map(final String help, final SardineException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, help);
        this.append(buffer, e.getMessage());
        // HTTP method status
        this.append(buffer, e.getResponsePhrase());
        if(e.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            return new LoginFailureException(buffer.toString(), e);
        }
        return new IOException(buffer.toString(), e);
    }
}
