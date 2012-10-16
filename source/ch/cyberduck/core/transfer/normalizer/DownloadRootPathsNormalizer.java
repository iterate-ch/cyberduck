package ch.cyberduck.core.transfer.normalizer;

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Id$
 */
public class DownloadRootPathsNormalizer implements RootPathsNormalizer<List<Path>> {
    private static final Logger log = Logger.getLogger(DownloadRootPathsNormalizer.class);

    @Override
    public List<Path> normalize(final List<Path> roots) {
        final List<Path> normalized = new Collection<Path>();
        for(final Path download : roots) {
            download.getSession().message(
                    MessageFormat.format(Locale.localizedString("Prepare {0}", "Status"), download.getName()));
            boolean duplicate = false;
            for(Iterator<Path> iter = normalized.iterator(); iter.hasNext(); ) {
                Path n = iter.next();
                if(download.isChild(n)) {
                    // The selected file is a child of a directory already included
                    duplicate = true;
                    break;
                }
                if(n.isChild(download)) {
                    iter.remove();
                }
                if(download.getLocal().equals(n.getLocal())) {
                    // The selected file has the same name; if downloaded as a root element
                    // it would overwrite the earlier
                    final String parent = download.getLocal().getParent().getAbsolute();
                    final String filename = download.getName();
                    String proposal;
                    int no = 0;
                    do {
                        no++;
                        proposal = FilenameUtils.getBaseName(filename) + "-" + no;
                        if(StringUtils.isNotBlank(FilenameUtils.getExtension(filename))) {
                            proposal += "." + FilenameUtils.getExtension(filename);
                        }
                        download.getLocal().setPath(parent, proposal);
                    }
                    while(download.getLocal().exists());
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Changed local name from %s to %s", filename, download.getName()));
                    }
                }
            }
            // Prunes the list of selected files. Files which are a child of an already included directory
            // are removed from the returned list.
            if(!duplicate) {
                normalized.add(download);
            }
        }
        return normalized;
    }
}
