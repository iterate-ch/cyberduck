package ch.cyberduck.core.exception;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.threading.BackgroundException;

/**
 * @version $Id$
 */
public interface IOExceptionMappingService<E> {

    BackgroundException map(final E exception, final Host host);

    /**
     * @param exception Service error
     * @return Mapped exception
     */
    BackgroundException map(String message, E exception, Host host);

    /**
     * @param exception Service error
     * @return Mapped exception
     */
    BackgroundException map(String message, E exception, Path directory);
}
