package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.transfer.SymlinkResolver;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class OverwriteFilter extends AbstractUploadFilter {
    private static final Logger log = Logger.getLogger(OverwriteFilter.class);

    public OverwriteFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }
}
