package ch.cyberduck.core.exception;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @version $Id$
 */
public abstract class AbstractIOExceptionMappingService<T extends Exception> implements IOExceptionMappingService<T> {
    private static Logger log = Logger.getLogger(AbstractIOExceptionMappingService.class);

    @Override
    public BackgroundException map(final T failure, final Host host) {
        return this.map("Connection failed", failure, host);
    }

    @Override
    public BackgroundException map(final T failure, final Path directory) {
        return this.map("Connection failed", failure, null, directory);
    }

    @Override
    public BackgroundException map(final String message, final T failure, final Host host) {
        return this.map(message, failure, host, null);
    }

    @Override
    public BackgroundException map(final String message, final T failure, final Path directory) {
        return this.map(message, failure, directory.getHost(), directory);
    }

    public BackgroundException map(final String message, final T failure, final Host host, final Path directory) {
        final IOException resolved = this.map(failure);
        return new BackgroundException(host, directory, message, resolved);
    }

    /**
     * @param exception Service error
     * @return Mapped exception
     */
    public abstract IOException map(T exception);

    protected StringBuilder append(final StringBuilder buffer, final String message) {
        if(StringUtils.isBlank(message)) {
            return buffer;
        }
        if(buffer.length() > 0) {
            buffer.append(" ");
        }
        buffer.append(message);
        if(buffer.charAt(buffer.length() - 1) == '.') {
            return buffer;
        }
        return buffer.append(".");
    }

    protected IOException wrap(final T e, final StringBuilder buffer) {
        final IOException failure = new IOException(buffer.toString());
        failure.initCause(e);
        return failure;
    }
}
