package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.SymlinkResolver;

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
    public void prepare(final Path file) {
        if(file.getLocal().exists() && file.getLocal().attributes().getSize() > 0) {
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
        super.prepare(file);
    }
}