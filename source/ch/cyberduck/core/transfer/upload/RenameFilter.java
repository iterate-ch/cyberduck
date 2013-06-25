package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class RenameFilter extends AbstractUploadFilter {
    private static final Logger log = Logger.getLogger(RenameFilter.class);

    public RenameFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Session session, final Path file) throws BackgroundException {
        return true;
    }

    @Override
    public TransferStatus prepare(final Session session, final Path file) throws BackgroundException {
        final TransferStatus status = super.prepare(session, file);
        if(file.exists()) {
            final Path parent = file.getParent();
            final String filename = file.getName();
            int no = 0;
            while(file.exists()) {
                no++;
                String proposal = FilenameUtils.getBaseName(filename) + "-" + no;
                if(StringUtils.isNotBlank(FilenameUtils.getExtension(filename))) {
                    proposal += "." + FilenameUtils.getExtension(filename);
                }
                file.setPath(parent, proposal);
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Changed local name from %s to %s", filename, file.getName()));
            }
        }
        return status;
    }
}
