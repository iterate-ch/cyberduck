package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.SymlinkResolver;

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
            file.status().setComplete(true);
            return false;
        }
        return super.accept(file);
    }

    @Override
    public void prepare(final Path file) {
        if(file.attributes().isFile()) {
            if(file.getLocal().exists()) {
                file.status().setResume(file.getLocal().attributes().getSize() > 0);
                file.status().setCurrent(file.getLocal().attributes().getSize());
            }
        }
        super.prepare(file);
    }
}