package ch.cyberduck.core.exception;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public abstract class AbstractIOExceptionMappingService<T extends Exception> implements IOExceptionMappingService<T> {
    private static Logger log = Logger.getLogger(AbstractIOExceptionMappingService.class);

    public BackgroundException map(final T failure, final Host host) {
        return this.map("Connection failed", failure, host);
    }

    public BackgroundException map(final String message, final T failure, final Host host) {
        return this.map(message, failure, host, null);
    }

    public BackgroundException map(final String message, final T failure, final Path directory) {
        return this.map(MessageFormat.format(StringUtils.chomp(message), directory.getName()), failure,
                directory.getHost(), directory);
    }

    private BackgroundException map(final String message, final T failure, final Host host, final Path directory) {
        final BackgroundException exception = this.map(failure);
        exception.setHost(host);
        exception.setPath(directory);
        exception.setTitle(message);
        return exception;
    }

    /**
     * @param exception Service error
     * @return Mapped exception
     */
    @Override
    public abstract BackgroundException map(T exception);

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

    protected BackgroundException wrap(final T e, final StringBuilder buffer) {
        return new BackgroundException(buffer.toString(), e);
    }
}
