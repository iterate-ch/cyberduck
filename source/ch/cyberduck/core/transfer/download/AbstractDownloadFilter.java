package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.transfer.TransferPathFilter;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public abstract class AbstractDownloadFilter extends TransferPathFilter {
    private static final Logger log = Logger.getLogger(AbstractDownloadFilter.class);

    private DownloadSymlinkResolver symlinkResolver;

    public AbstractDownloadFilter(final DownloadSymlinkResolver symlinkResolver) {
        this.symlinkResolver = symlinkResolver;
    }

    @Override
    public boolean accept(final Path file) {
        if(file.attributes().isSymbolicLink()) {
            if(!symlinkResolver.resolve(file)) {
                return symlinkResolver.include(file);
            }
        }
        final Local volume = file.getLocal().getVolume();
        if(!volume.exists()) {
            log.warn(String.format("Volume %s not mounted", volume.getAbsolute()));
            return false;
        }
        return true;
    }

    @Override
    public void prepare(final Path file) {
        if(file.attributes().getSize() == -1) {
            file.readSize();
        }
        if(file.getSession().isReadTimestampSupported()) {
            if(file.attributes().getModificationDate() == -1) {
                if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
                    file.readTimestamp();
                }
            }
        }
        if(file.getSession().isUnixPermissionsSupported()) {
            if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
                if(file.attributes().getPermission().equals(Permission.EMPTY)) {
                    file.readUnixPermission();
                }
            }
        }
        if(file.attributes().isFile()) {
            if(file.attributes().isFile()) {
                if(file.attributes().isSymbolicLink()) {
                    if(symlinkResolver.resolve(file)) {
                        // No file size increase for symbolic link to be created locally
                    }
                    else {
                        // A server will resolve the symbolic link when the file is requested.
                        final Path target = (Path) file.getSymlinkTarget();
                        if(target.attributes().getSize() == -1) {
                            target.readSize();
                        }
                        file.status().setLength(target.attributes().getSize());
                    }
                }
                else {
                    // Read file size
                    file.status().setLength(file.attributes().getSize());
                }
            }
        }
        if(!file.getLocal().getParent().exists()) {
            // Create download folder if missing
            file.getLocal().getParent().mkdir(true);
        }
    }

    /**
     * Update timestamp and permission
     */
    @Override
    public void complete(final Path file) {
        if(!file.status().isCanceled()) {
            if(Preferences.instance().getBoolean("queue.download.changePermissions")) {
                Permission permission = Permission.EMPTY;
                if(Preferences.instance().getBoolean("queue.download.permissions.useDefault")) {
                    if(file.attributes().isFile()) {
                        permission = new Permission(
                                Preferences.instance().getInteger("queue.download.permissions.file.default"));
                    }
                    if(file.attributes().isDirectory()) {
                        permission = new Permission(
                                Preferences.instance().getInteger("queue.download.permissions.folder.default"));
                    }
                }
                else {
                    permission = file.attributes().getPermission();
                }
                if(!Permission.EMPTY.equals(permission)) {
                    if(file.attributes().isDirectory()) {
                        // Make sure we can read & write files to directory created.
                        permission.getOwnerPermissions()[Permission.READ] = true;
                        permission.getOwnerPermissions()[Permission.WRITE] = true;
                        permission.getOwnerPermissions()[Permission.EXECUTE] = true;
                    }
                    if(file.attributes().isFile()) {
                        // Make sure the owner can always read and write.
                        permission.getOwnerPermissions()[Permission.READ] = true;
                        permission.getOwnerPermissions()[Permission.WRITE] = true;
                    }
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Updating permissions of %s to %s", file.getLocal(), permission));
                    }
                    file.getLocal().writeUnixPermission(permission, false);
                }
            }
            if(Preferences.instance().getBoolean("queue.download.preserveDate")) {
                if(file.attributes().getModificationDate() != -1) {
                    long timestamp = file.attributes().getModificationDate();
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Updating timestamp of %s to %d", file.getLocal(), timestamp));
                    }
                    file.getLocal().writeTimestamp(-1, timestamp, -1);
                }
            }
        }
    }
}
