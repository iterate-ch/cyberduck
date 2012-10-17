package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class AbstractUploadFilter extends TransferPathFilter {
    private static final Logger log = Logger.getLogger(AbstractUploadFilter.class);

    private SymlinkResolver symlinkResolver;

    public AbstractUploadFilter(final SymlinkResolver symlinkResolver) {
        this.symlinkResolver = symlinkResolver;
    }

    @Override
    public boolean accept(final Path file) {
        if(file.attributes().isDirectory()) {
            // Do not attempt to create a directory that already exists
            if(file.exists()) {
                return false;
            }
        }
        if(file.attributes().isFile()) {
            if(!file.getLocal().exists()) {
                // Local file is no more here
                return false;
            }
            if(file.getLocal().attributes().isSymbolicLink()) {
                if(!symlinkResolver.resolve(file)) {
                    return symlinkResolver.include(file);
                }
            }
        }
        return true;
    }

    @Override
    public TransferStatus prepare(final Path file) {
        if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
            if(file.exists()) {
                // Do not overwrite permissions for existing file.
                if(file.getSession().isUnixPermissionsSupported()) {
                    file.readUnixPermission();
                }
                // Do not overwrite ACL for existing file.
                if(file.getSession().isAclSupported()) {
                    file.readAcl();
                }
            }
            else {
                if(file.getSession().isUnixPermissionsSupported()) {
                    if(Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                        if(file.attributes().isFile()) {
                            file.attributes().setPermission(new Permission(
                                    Preferences.instance().getInteger("queue.upload.permissions.file.default")));
                        }
                        else if(file.attributes().isDirectory()) {
                            file.attributes().setPermission(new Permission(
                                    Preferences.instance().getInteger("queue.upload.permissions.folder.default")));
                        }
                    }
                    else {
                        // Read permissions from local file
                        file.attributes().setPermission(file.getLocal().attributes().getPermission());
                    }
                }
                if(file.getSession().isAclSupported()) {
                    // ACL set on object creation with default from Preferences
                }
            }
        }
        final TransferStatus status = new TransferStatus();
        if(file.attributes().isFile()) {
            if(file.getLocal().attributes().isSymbolicLink()) {
                if(symlinkResolver.resolve(file)) {
                    // No file size increase for symbolic link to be created on the server
                }
                else {
                    // Will resolve the symbolic link when the file is requested.
                    final AbstractPath target = file.getLocal().getSymlinkTarget();
                    status.setLength(target.attributes().getSize());
                }
            }
            else {
                // Read file size from filesystem
                status.setLength(file.getLocal().attributes().getSize());
            }
        }
        if(file.attributes().isDirectory()) {
            if(!file.exists()) {
                file.getSession().cache().put(file.getReference(), AttributedList.<Path>emptyList());
            }
        }
        return status;
    }

    @Override
    public void complete(final Path file, final TransferStatus status) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Complete %s with status %s", file.getAbsolute(), status));
        }
        if(!status.isCanceled()) {
            if(file.getSession().isUnixPermissionsSupported()) {
                if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                    Permission permission = file.attributes().getPermission();
                    if(!Permission.EMPTY.equals(permission)) {
                        file.writeUnixPermission(permission);
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