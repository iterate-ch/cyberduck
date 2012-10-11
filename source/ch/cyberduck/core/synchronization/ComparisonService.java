package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.Path;

/**
 * @version $Id$
 */
public interface ComparisonService {

    Comparison compare(Path p);
}
