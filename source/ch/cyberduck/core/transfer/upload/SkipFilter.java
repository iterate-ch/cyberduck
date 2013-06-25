package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

/**
 * @version $Id$
 */
public class SkipFilter extends AbstractUploadFilter {

    public SkipFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    /**
     * Skip files that already exist on the server.
     */
    @Override
    public boolean accept(final Session session, final Path file) throws BackgroundException {
        if(file.exists()) {
            return false;
        }
        return super.accept(session, file);
    }
}
