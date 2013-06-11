package ch.cyberduck.core.exception;

import org.apache.http.HttpException;

import java.io.IOException;

/**
 * @version $Id$
 */
public class HttpExceptionMappingService extends AbstractIOExceptionMappingService<HttpException> {

    @Override
    public IOException map(final HttpException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        return this.wrap(e, buffer);
    }
}
