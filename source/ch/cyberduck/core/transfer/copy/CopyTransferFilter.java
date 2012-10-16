package ch.cyberduck.core.transfer.copy;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.DownloadSymlinkResolver;
import ch.cyberduck.core.transfer.upload.AbstractUploadFilter;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;

/**
 * @version $Id$
 */
public class CopyTransferFilter extends AbstractUploadFilter {
    private static final Logger log = Logger.getLogger(CopyTransferFilter.class);

    private final Map<Path, Path> files;

    public CopyTransferFilter(final Map<Path, Path> files) {
        super(new DownloadSymlinkResolver(new ArrayList<Path>(files.keySet())));
        this.files = files;
    }

    @Override
    public boolean accept(final Path source) {
        if(source.attributes().isDirectory()) {
            final Path destination = files.get(source);
            // Do not attempt to create a directory that already exists
            if(destination.exists()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Path source) {
        final Path destination = files.get(source);
        destination.setAttributes(source.attributes());
        final TransferStatus status = new TransferStatus();
        if(source.attributes().isFile()) {
            if(source.attributes().getSize() == -1) {
                // Read file size
                source.readSize();
            }
            final long length = source.attributes().getSize();
            // Download + Upload
            status.setLength(length * 2);
        }
        else if(source.attributes().isDirectory()) {
            if(!destination.exists()) {
                files.get(source).getSession().cache().put(destination.getReference(), AttributedList.<Path>emptyList());
            }
        }
        return status;
    }

    @Override
    public void complete(Path source, final TransferStatus status) {
        final Path destination = files.get(source);
        super.complete(destination, status);
    }
}