package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.SymlinkResolver;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class ResumeFilter extends AbstractUploadFilter {
    private static final Logger log = Logger.getLogger(ResumeFilter.class);

    public ResumeFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    /**
     * Append to existing file.
     */
    @Override
    public void prepare(final Path file) {
        if(file.attributes().isFile()) {
            if(file.exists()) {
                // Do not trust cached value which is from last directory listing
                // and possibly outdated. Fix #3284.
                file.readSize();
                if(file.getLocal().attributes().getSize() == file.attributes().getSize()) {
                    // No need to resume completed transfers
                    file.status().setComplete(true);
                }
                else {
                    if(file.attributes().getSize() > 0) {
                        file.status().setResume(true);
                        file.status().setCurrent(file.attributes().getSize());
                    }
                }
            }
        }
        super.prepare(file);
    }
}
