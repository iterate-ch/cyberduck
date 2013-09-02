package ch.cyberduck.core.features;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

/**
 * @version $Id:$
 */
public interface Find {

    /**
     * Check for file existence. The default implementation does a directory listing of the parent folder.
     */
    boolean find(Path file) throws BackgroundException;
}
