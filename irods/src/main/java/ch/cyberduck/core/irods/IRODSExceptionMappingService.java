package ch.cyberduck.core.irods;

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IRODSExceptionMappingService extends AbstractExceptionMappingService<Exception> {
    private static final Logger log = LogManager.getLogger(IRODSExceptionMappingService.class);

    @Override
    public BackgroundException map(final Exception e) {
        //TODO: write a more complete exception mapping services
        log.warn("Map failure {}", e.toString());
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        return this.wrap(e, buffer);
    }

}
