package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Path;
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

    @Override
    public boolean accept(final Path file) {
        if(file.attributes().isDirectory()) {
            // Do not attempt to create a directory that already exists
            if(file.exists()) {
                return false;
            }
        }
        return super.accept(file);
    }

    @Override
    public void prepare(final Path file) {
        if(file.attributes().isFile()) {
            file.status().setResume(false);
        }
        super.prepare(file);
    }
}
