package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Path;

/**
 * @version $Id:$
 */
public class SkipFilter extends AbstractUploadFilter {

    public SkipFilter(final UploadSymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    /**
     * Skip files that already exist on the server.
     */
    @Override
    public boolean accept(final Path file) {
        if(file.exists()) {
            // Set completion status for skipped files
            file.status().setComplete(true);
            return false;
        }
        return super.accept(file);
    }
}
