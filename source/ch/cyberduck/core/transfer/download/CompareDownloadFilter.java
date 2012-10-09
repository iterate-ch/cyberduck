package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.synchronization.CompareService;
import ch.cyberduck.core.synchronization.Comparison;

/**
 * @version $Id:$
 */
public class CompareDownloadFilter extends AbstractDownloadFilter {

    private CompareService compareService = new CompareService();

    public CompareDownloadFilter(final DownloadSymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path file) {
        final Comparison comparison = compareService.compare(file);
        switch(comparison) {
            case LOCAL_NEWER:
            case EQUAL:
                return false;
            case REMOTE_NEWER:
                return true;
        }
        return super.accept(file);
    }
}
