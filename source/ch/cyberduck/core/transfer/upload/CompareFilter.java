package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CompareFilter extends AbstractUploadFilter {
    private static final Logger log = Logger.getLogger(CompareFilter.class);

    private ComparisonService compareService = new ComparisonService();

    public CompareFilter(final UploadSymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path file) {
        final Comparison comparison = compareService.compare(file);
        switch(comparison) {
            case LOCAL_NEWER:
            case EQUAL:
                return super.accept(file);
            case REMOTE_NEWER:
                return false;
        }
        log.warn(String.format("Invalid comparison result %s", comparison));
        return false;
    }
}
