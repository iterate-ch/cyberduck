package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class RenameFilter extends AbstractDownloadFilter {
    private static final Logger log = Logger.getLogger(RenameFilter.class);

    public RenameFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path file) {
        return true;
    }

    @Override
    public TransferStatus prepare(final Path file) {
        if(file.getLocal().exists()) {
            final String parent = file.getLocal().getParent().getAbsolute();
            final String filename = file.getName();
            int no = 0;
            while(file.getLocal().exists()) {
                no++;
                String proposal = FilenameUtils.getBaseName(filename) + "-" + no;
                if(StringUtils.isNotBlank(FilenameUtils.getExtension(filename))) {
                    proposal += "." + FilenameUtils.getExtension(filename);
                }
                file.setLocal(LocalFactory.createLocal(parent, proposal));
            }
            if(log.isInfoEnabled()) {
                log.info(String.format("Changed local name from %s to %s", filename, file.getLocal().getName()));
            }
        }
        return super.prepare(file);
    }
}