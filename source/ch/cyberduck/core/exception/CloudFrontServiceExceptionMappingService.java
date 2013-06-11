package ch.cyberduck.core.exception;

import org.apache.http.HttpStatus;
import org.jets3t.service.CloudFrontServiceException;

import java.io.IOException;

/**
 * @version $Id$
 */
public class CloudFrontServiceExceptionMappingService extends AbstractIOExceptionMappingService<CloudFrontServiceException> {

    @Override
    public IOException map(final CloudFrontServiceException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getErrorMessage());
        this.append(buffer, e.getErrorDetail());
        if(e.getResponseCode() == HttpStatus.SC_FORBIDDEN) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e.getResponseCode() == HttpStatus.SC_BAD_REQUEST) {
            if(e.getErrorCode().equals("InvalidHttpAuthHeader")) {
                return new LoginFailureException(buffer.toString(), e);
            }
        }
        return new IOException(buffer.toString(), e);
    }
}
