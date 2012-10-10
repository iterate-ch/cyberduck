package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.SymlinkResolver;

/**
 * @version $Id$
 */
public class OverwriteFilter extends AbstractDownloadFilter {

    public OverwriteFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path file) {
        if(file.attributes().isDirectory()) {
            if(file.getLocal().exists()) {
                return false;
            }
        }
        return super.accept(file);
    }
}
