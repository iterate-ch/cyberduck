package ch.cyberduck.core.exception;

import java.io.IOException;

/**
 * @version $Id$
 */
public interface IOExceptionMappingService<E> {

    /**
     * @param message   Help message
     * @param exception Service error
     * @return Mapped exception
     */
    IOException map(String message, E exception);

    /**
     * @param exception Service error
     * @return Mapped exception
     */
    IOException map(E exception);
}
