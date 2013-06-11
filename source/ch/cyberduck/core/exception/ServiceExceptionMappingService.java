package ch.cyberduck.core.exception;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.jets3t.service.ServiceException;

import java.io.IOException;

/**
 * @version $Id$
 */
public class ServiceExceptionMappingService extends AbstractIOExceptionMappingService<ServiceException> {

    @Override
    public IOException map(final ServiceException e) {
        final StringBuilder buffer = new StringBuilder();
        if(StringUtils.isEmpty(e.getErrorMessage())) {
            if(null == e.getCause()) {
                this.append(buffer, e.getMessage());
            }
            else {
                this.append(buffer, e.getCause().getMessage());
            }
        }
        else {
            // S3 protocol message
            this.append(buffer, e.getErrorMessage());
        }
        if(HttpStatus.SC_FORBIDDEN == e.getResponseCode()) {
            return new LoginFailureException(buffer.toString(), e);
        }
        else if(HttpStatus.SC_UNAUTHORIZED == e.getResponseCode()) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e.getCause() instanceof HttpResponseException) {
            return new HttpResponseExceptionMappingService().map((HttpResponseException) e.getCause());
        }
        return this.wrap(e, buffer);
    }
}