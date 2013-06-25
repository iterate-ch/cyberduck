package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

/**
 * @version $Id$
 */
public class SkipFilter extends AbstractDownloadFilter {

    public SkipFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Session session, final Path file) throws BackgroundException {
        if(file.getLocal().exists()) {
            return false;
        }
        return super.accept(session, file);
    }
}