package ch.cyberduck.core.exception;

import ch.cyberduck.core.threading.BackgroundException;

import org.apache.http.HttpStatus;
import org.jets3t.service.ServiceException;

import java.io.IOException;

/**
 * @version $Id$
 */
public class ServiceExceptionMappingService extends AbstractIOExceptionMappingService<ServiceException> {

    @Override
    public BackgroundException map(final ServiceException e) {
        final StringBuilder buffer = new StringBuilder();
        if(e.isParsedFromXmlMessage()) {
            // S3 protocol message
            this.append(buffer, e.getErrorMessage());
            if(HttpStatus.SC_FORBIDDEN == e.getResponseCode()) {
                return new LoginFailureException(buffer.toString(), e);
            }
            else if(HttpStatus.SC_UNAUTHORIZED == e.getResponseCode()) {
                return new LoginFailureException(buffer.toString(), e);
            }
            else if(e.getErrorCode() != null) {
                if(e.getErrorCode().equals("InvalidAccessKeyId") // Invalid Access ID
                        || e.getErrorCode().equals("SignatureDoesNotMatch")) { // Invalid Secret Key
                    return new LoginFailureException(buffer.toString(), e);
                }
            }
            return this.wrap(e, buffer);
        }
        else {
            if(e.getCause() instanceof IOException) {
                return new DefaultIOExceptionMappingService().map((IOException) e.getCause());
            }
            if(null == e.getCause()) {
                this.append(buffer, e.getMessage());
            }
            else {
                this.append(buffer, e.getCause().getMessage());
            }
            return this.wrap(e, buffer);
        }
    }
}