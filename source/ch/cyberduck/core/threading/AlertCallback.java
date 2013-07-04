package ch.cyberduck.core.threading;

import ch.cyberduck.core.exception.BackgroundException;

/**
 * @version $Id$
 */
public interface AlertCallback {

    void alert(RepeatableBackgroundAction action,
               BackgroundException failure, StringBuilder transcript);
}
