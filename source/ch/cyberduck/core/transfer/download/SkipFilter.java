package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;

/**
 * @version $Id:$
 */
public class SkipFilter extends AbstractDownloadFilter {

    public SkipFilter(final DownloadSymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path file) {
        if(file.getLocal().exists()) {
            // Set completion status for skipped files
            file.status().setComplete(true);
            return false;
        }
        return super.accept(file);
    }
}