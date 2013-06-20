package ch.cyberduck.core.exception;

import ch.cyberduck.core.threading.BackgroundException;

/**
 * @version $Id$
 */
public interface IOExceptionMappingService<E> {

    BackgroundException map(E exception);
}
