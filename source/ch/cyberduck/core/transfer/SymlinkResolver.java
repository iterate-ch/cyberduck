package ch.cyberduck.core.transfer;

import ch.cyberduck.core.Path;

/**
 * @version $Id:$
 */
public interface SymlinkResolver {

    /**
     * @param file Symbolic link
     * @return True if the symbolic link target can be resolved on transfer target
     */
    boolean resolve(Path file);

    /**
     * @param file Symbolic link
     * @return False if symlink target is already included as a child in the root files
     */
    boolean include(Path file);
}
