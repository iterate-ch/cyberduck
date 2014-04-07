package ch.cyberduck.core.threading;

import ch.cyberduck.core.exception.LoginCanceledException;

/**
 * @version $Id$
 */
public interface CancelCallback {
    void verify() throws LoginCanceledException;
}
