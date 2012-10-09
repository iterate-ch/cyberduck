package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CompareFilter extends AbstractDownloadFilter {
    private static final Logger log = Logger.getLogger(CompareFilter.class);

    private ComparisonService compareService = new ComparisonService();

    public CompareFilter(final DownloadSymlinkResolver symlinkResolver) {
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
                return super.accept(file);
        }
        log.warn(String.format("Invalid comparison result %s", comparison));
        return false;
    }
}
