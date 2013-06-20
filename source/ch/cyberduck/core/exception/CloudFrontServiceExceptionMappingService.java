package ch.cyberduck.core.exception;

import ch.cyberduck.core.threading.BackgroundException;

import org.apache.http.HttpStatus;
import org.jets3t.service.CloudFrontServiceException;

/**
 * @version $Id$
 */
public class CloudFrontServiceExceptionMappingService extends AbstractIOExceptionMappingService<CloudFrontServiceException> {

    @Override
    public BackgroundException map(final CloudFrontServiceException e) {
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
        return this.wrap(e, buffer);
    }
}
