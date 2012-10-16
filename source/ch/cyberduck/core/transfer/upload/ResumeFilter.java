package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

/**
 * @version $Id$
 */
public class ResumeFilter extends AbstractUploadFilter {

    public ResumeFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    /**
     * Append to existing file.
     */
    @Override
    public TransferStatus prepare(final Path file) {
        final TransferStatus status = super.prepare(file);
        if(file.attributes().isFile()) {
            if(file.exists()) {
                // Do not trust cached value which is from last directory listing
                // and possibly outdated. Fix #3284.
                file.readSize();
                if(file.getLocal().attributes().getSize() == file.attributes().getSize()) {
                    // No need to resume completed transfers
                    status.setComplete();
                }
                else {
                    if(file.attributes().getSize() > 0) {
                        status.setResume(true);
                        status.setCurrent(file.attributes().getSize());
                    }
                }
            }
        }
        return status;
    }
}
