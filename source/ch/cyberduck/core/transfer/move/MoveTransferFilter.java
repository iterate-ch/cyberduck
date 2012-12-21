package ch.cyberduck.core.transfer.move;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @version $Id$
 */
public class MoveTransferFilter extends TransferPathFilter {
    private static final Logger log = Logger.getLogger(MoveTransferFilter.class);

    private final Map<Path, Path> files;

    public MoveTransferFilter(final Map<Path, Path> files) {
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
        final TransferStatus status = new TransferStatus();
        if(source.attributes().isFile()) {
            status.setLength(source.attributes().getSize());
        }
        return status;
    }

    @Override
    public void complete(final Path file, final TransferOptions options, final TransferStatus status) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Complete %s with status %s", file.getAbsolute(), status));
        }
    }
}