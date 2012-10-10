package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.SymlinkResolver;

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
    public boolean accept(final Path file) {
        if(file.exists()) {
            return false;
        }
        return super.accept(file);
    }

    @Override
    public void prepare(final Path file) {
        if(file.exists()) {
            // Set completion status for skipped files
            file.status().setComplete(true);
        }
        super.prepare(file);
    }
}
