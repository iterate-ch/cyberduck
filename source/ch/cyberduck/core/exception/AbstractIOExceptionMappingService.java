package ch.cyberduck.core.exception;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * @version $Id$
 */
public abstract class AbstractIOExceptionMappingService<T> implements IOExceptionMappingService<T> {

    @Override
    public BackgroundException map(final String message, final T exception, final Host host) {
        return new BackgroundException(host, message, this.map(exception));
    }

    @Override
    public BackgroundException map(final String message, final T exception, final Path directory) {
        return new BackgroundException(directory.getHost(), directory, message, this.map(exception));
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
}
