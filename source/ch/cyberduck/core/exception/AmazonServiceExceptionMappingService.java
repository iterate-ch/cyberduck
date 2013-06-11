package ch.cyberduck.core.exception;

import java.io.IOException;

import com.amazonaws.AmazonServiceException;

/**
 * @version $Id$
 */
public class AmazonServiceExceptionMappingService extends AbstractIOExceptionMappingService<AmazonServiceException> {

    @Override
    public IOException map(final AmazonServiceException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        this.append(buffer, e.getErrorCode());
        return this.wrap(e, buffer);
    }
}
