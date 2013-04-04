package ch.cyberduck.core.transfer.copy;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

/**
 * @version $Id$
 */
public class CopyTransferFilter extends TransferPathFilter {

    private final Map<Path, Path> files;

    public CopyTransferFilter(final Map<Path, Path> files) {
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
            if(source.attributes().getSize() == -1) {
                // Read file size
                source.readSize();
            }
            status.setLength(source.attributes().getSize());
        }
        return status;
    }

    @Override
    public void complete(final Path source, final TransferOptions options, final TransferStatus status) {
        if(status.isComplete()) {
            final Path destination = files.get(source);
            if(destination.getSession().isUnixPermissionsSupported()) {
                if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                    Permission permission = source.attributes().getPermission();
                    if(!Permission.EMPTY.equals(permission)) {
                        destination.writeUnixPermission(permission);
                    }
                }
            }
            if(destination.getSession().isWriteTimestampSupported()) {
                if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                    destination.writeTimestamp(source.attributes().getCreationDate(),
                            source.attributes().getModificationDate(),
                            source.attributes().getAccessedDate());
                }
            }
        }
    }
}