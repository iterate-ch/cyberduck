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

    @Override
    public boolean accept(final Path file) {
        if(file.attributes().isFile()) {
            if(file.getLocal().attributes().getSize() == file.attributes().getSize()) {
                // No need to resume completed transfers
                file.status().setComplete(true);
                return false;
            }
        }
        return super.accept(file);
    }

    /**
     * Append to existing file.
     */
    @Override
    public void prepare(final Path file) {
        if(file.exists()) {
            if(file.attributes().getSize() == -1) {
                file.readSize();
            }
        }
        if(file.attributes().isFile()) {
            boolean resume = file.isUploadResumable();
            file.status().setResume(resume);
            if(resume) {
                if(file.attributes().getSize() == -1) {
                    log.warn("Unknown remote size for:" + file.getAbsolute());
                }
                else {
                    file.status().setCurrent(file.attributes().getSize());
                }
            }
        }
        super.prepare(file);
    }
}
