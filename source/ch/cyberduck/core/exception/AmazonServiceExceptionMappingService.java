package ch.cyberduck.core.exception;

import ch.cyberduck.core.threading.BackgroundException;

import com.amazonaws.AmazonServiceException;

/**
 * @version $Id$
 */
public class AmazonServiceExceptionMappingService extends AbstractIOExceptionMappingService<AmazonServiceException> {

    @Override
    public BackgroundException map(final AmazonServiceException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        this.append(buffer, e.getErrorCode());
        return this.wrap(e, buffer);
    }
}
