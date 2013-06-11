package ch.cyberduck.core.exception;

import java.io.IOException;

import ch.ethz.ssh2.SFTPException;

/**
 * @version $Id$
 */
public class SFTPExceptionMappingService extends AbstractIOExceptionMappingService<SFTPException> {

    @Override
    public IOException map(final SFTPException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        this.append(buffer, e.getServerErrorMessage());
        return this.wrap(e, buffer);
    }
}
