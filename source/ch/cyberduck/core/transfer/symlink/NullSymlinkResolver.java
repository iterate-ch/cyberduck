package ch.cyberduck.core.transfer.symlink;

import ch.cyberduck.core.Path;

/**
 * @version $Id$
 */
public class NullSymlinkResolver extends AbstractSymlinkResolver {

    @Override
    public boolean resolve(final Path file) {
        return false;
    }

    @Override
    public boolean include(final Path file) {
        return true;
    }
}
