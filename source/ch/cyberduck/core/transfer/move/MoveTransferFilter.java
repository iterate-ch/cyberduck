package ch.cyberduck.core.transfer.move;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

/**
 * @version $Id$
 */
public class MoveTransferFilter extends TransferPathFilter {

    private Map<Path, Path> files;

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
        else if(source.attributes().isDirectory()) {
            final Path destination = files.get(source);
            if(!destination.exists()) {
                destination.getSession().cache().put(destination.getReference(), AttributedList.<Path>emptyList());
            }
        }
        return status;
    }

    @Override
    public void complete(Path p, final TransferStatus status) {
        //
    }
}