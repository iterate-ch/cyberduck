package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

/**
 * @version $Id$
 */
public class ResumeFilter extends AbstractDownloadFilter {

    public ResumeFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path file) {
        if(file.attributes().isDirectory()) {
            if(file.getLocal().exists()) {
                return false;
            }
        }
        if(file.getLocal().attributes().getSize() >= file.attributes().getSize()) {
            // No need to resume completed transfers
            return false;
        }
        return super.accept(file);
    }

    @Override
    public TransferStatus prepare(final Path file) {
        final TransferStatus status = super.prepare(file);
        if(file.getSession().isDownloadResumable()) {
            if(file.attributes().isFile()) {
                if(file.getLocal().exists()) {
                    if(file.attributes().getSize() > 0) {
                        status.setResume(true);
                        status.setCurrent(file.getLocal().attributes().getSize());
                    }
                }
            }
        }
        return status;
    }
}