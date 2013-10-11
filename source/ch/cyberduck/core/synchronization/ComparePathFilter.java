package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

/**
 * @version $Id:$
 */
public interface ComparePathFilter {
    public Comparison compare(final Path file) throws BackgroundException;
}
