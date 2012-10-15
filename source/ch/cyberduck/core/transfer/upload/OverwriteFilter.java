package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.transfer.SymlinkResolver;

/**
 * @version $Id$
 */
public class OverwriteFilter extends AbstractUploadFilter {

    public OverwriteFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }
}
