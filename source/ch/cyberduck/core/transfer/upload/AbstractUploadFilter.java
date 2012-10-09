package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.transfer.TransferPathFilter;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public abstract class AbstractUploadFilter extends TransferPathFilter {
    private static final Logger log = Logger.getLogger(AbstractUploadFilter.class);

    private UploadSymlinkResolver symlinkResolver;

    public AbstractUploadFilter(final UploadSymlinkResolver symlinkResolver) {
        this.symlinkResolver = symlinkResolver;
    }

    @Override
    public boolean accept(final Path file) {
        if(!file.getLocal().exists()) {
            return false;
        }
        if(file.getLocal().attributes().isSymbolicLink()) {
            if(!symlinkResolver.resolve(file)) {
                return symlinkResolver.include(file);
            }
        }
        return true;
    }

    @Override
    public void prepare(final Path file) {
        if(file.attributes().isFile()) {
            if(file.attributes().isFile()) {
                if(file.getLocal().attributes().isSymbolicLink()) {
                    if(symlinkResolver.resolve(file)) {
                        // No file size increase for symbolic link to be created on the server
                    }
                    else {
                        // Will resolve the symbolic link when the file is requested.
                        final AbstractPath target = file.getLocal().getSymlinkTarget();
                        file.status().setLength(target.attributes().getSize());
                    }
                }
                else {
                    // Read file size from filesystem
                    file.status().setLength(file.getLocal().attributes().getSize());
                }
            }
        }
        if(file.attributes().isDirectory()) {
            if(!file.exists()) {
                file.getSession().cache().put(file.getReference(), AttributedList.<Path>emptyList());
            }
        }
    }

    @Override
    public void complete(final Path file) {
        if(!file.status().isCanceled()) {
            if(file.getSession().isAclSupported()) {
                // Currently handled in S3 only.
            }
            if(file.getSession().isUnixPermissionsSupported()) {
                if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                    Permission permission = file.attributes().getPermission();
                    if(!Permission.EMPTY.equals(permission)) {
                        file.writeUnixPermission(permission, false);
                    }
                }
            }
            if(file.getSession().isWriteTimestampSupported()) {
                if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                    // Read timestamps from local file
                    file.writeTimestamp(file.getLocal().attributes().getCreationDate(),
                            file.getLocal().attributes().getModificationDate(),
                            file.getLocal().attributes().getAccessedDate());
                }
            }
        }
    }
}