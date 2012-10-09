package ch.cyberduck.core.transfer.move;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferPathFilter;

import java.util.Map;

/**
 * @version $Id:$
 */
public class MoveTransferFilter extends TransferPathFilter {

    private Map<Path, Path> files;

    public MoveTransferFilter(final Map<Path, Path> files) {
        this.files = files;
    }

    @Override
    public boolean accept(final Path source) {
        final Path destination = files.get(source);
        if(destination.attributes().isDirectory()) {
            // Do not attempt to create a directory that already exists
            if(destination.exists()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void prepare(final Path source) {
        if(source.attributes().isFile()) {
            source.status().setResume(false);
        }
        if(source.attributes().isFile()) {
            final long length = source.attributes().getSize();
            // Download
            source.status().setLength(length);
        }
        final Path dest = files.get(source);
        if(dest.attributes().isDirectory()) {
            if(!dest.exists()) {
                source.getSession().cache().put(dest.getReference(), AttributedList.<Path>emptyList());
            }
        }
    }

    @Override
    public void complete(Path p) {
        //
    }
}
