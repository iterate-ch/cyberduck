package ch.cyberduck.core.threading;

import ch.cyberduck.core.exception.ConnectionCanceledException;

/**
 * @version $Id$
 */
public interface CancelCallback {
    void verify() throws ConnectionCanceledException;
}
