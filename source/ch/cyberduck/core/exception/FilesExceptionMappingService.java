package ch.cyberduck.core.exception;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import java.io.IOException;

import com.rackspacecloud.client.cloudfiles.FilesException;

/**
 * @version $Id$
 */
public class FilesExceptionMappingService extends AbstractIOExceptionMappingService<FilesException> {

    @Override
    public IOException map(final FilesException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        final StatusLine status = e.getHttpStatusLine();
        if(null != status) {
            this.append(buffer, String.format("%d %s", status.getStatusCode(), status.getReasonPhrase()));
        }
        if(e.getHttpStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e.getHttpStatusCode() == HttpStatus.SC_FORBIDDEN) {
            return new LoginFailureException(buffer.toString(), e);
        }
        return this.wrap(e, buffer);
    }
}