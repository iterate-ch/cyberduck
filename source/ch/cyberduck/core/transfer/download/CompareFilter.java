package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.synchronization.CombinedComparisionService;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CompareFilter extends AbstractDownloadFilter {
    private static final Logger log = Logger.getLogger(CompareFilter.class);

    private ComparisonService compareService = new CombinedComparisionService();

    public CompareFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Session session, final Path file) throws BackgroundException {
        if(super.accept(session, file)) {
            final Comparison comparison = compareService.compare(file);
            switch(comparison) {
                case LOCAL_NEWER:
                case EQUAL:
                    return false;
                case REMOTE_NEWER:
                    return super.accept(session, file);
            }
            log.warn(String.format("Invalid comparison result %s", comparison));
        }
        return false;
    }
}
