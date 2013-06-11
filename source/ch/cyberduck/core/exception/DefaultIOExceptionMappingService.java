package ch.cyberduck.core.exception;

import java.io.IOException;

/**
 * @version $Id:$
 */
public class DefaultIOExceptionMappingService extends AbstractIOExceptionMappingService<IOException> {

    @Override
    public IOException map(final IOException failure) {
        return failure;
    }
}
