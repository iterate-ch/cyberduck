package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;

/**
 * @version $Id:$
 */
public class OverwriteFilter extends AbstractDownloadFilter {

    public OverwriteFilter(final DownloadSymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path file) {
        if(file.attributes().isDirectory()) {
            if(file.getLocal().exists()) {
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
