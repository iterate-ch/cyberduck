package ch.cyberduck.core.transfer;

import ch.cyberduck.core.Path;

import java.util.List;

/**
 * @version $Id:$
 */
public interface RootPathsNormalizer {
    public List<Path> normalize(List<Path> roots);
}
