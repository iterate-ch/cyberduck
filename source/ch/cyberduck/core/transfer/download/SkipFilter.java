package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.SymlinkResolver;

/**
 * @version $Id$
 */
public class SkipFilter extends AbstractDownloadFilter {

    public SkipFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path file) {
        if(file.getLocal().exists()) {
            return false;
        }
        return super.accept(file);
    }

    @Override
    public void prepare(final Path file) {
        if(file.getLocal().exists()) {
            // Set completion status for skipped files
            file.status().setComplete(true);
        }
        super.prepare(file);
    }
}