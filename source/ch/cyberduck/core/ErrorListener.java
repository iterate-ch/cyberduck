package ch.cyberduck.core;

import ch.cyberduck.ui.cocoa.threading.BackgroundException;

/**
 * @version $Id$
 */
public interface ErrorListener {

    /**
     *
     * @param exception
     */
    abstract void error(final BackgroundException exception);

}
