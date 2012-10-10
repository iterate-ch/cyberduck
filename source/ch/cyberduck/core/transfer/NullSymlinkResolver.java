package ch.cyberduck.core.transfer;

import ch.cyberduck.core.Path;

/**
 * @version $Id:$
 */
public class NullSymlinkResolver implements SymlinkResolver {

    @Override
    public boolean resolve(final Path file) {
        return false;
    }

    @Override
    public boolean include(final Path file) {
        return true;
    }
}
