package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.transfer.SymlinkResolver;

/**
 * @version $Id$
 */
public class OverwriteFilter extends AbstractDownloadFilter {

    public OverwriteFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }
}
