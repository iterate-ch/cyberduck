package ch.cyberduck.core.transfer.symlink;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;

import java.util.List;

/**
 * @version $Id$
 */
public class DownloadSymlinkResolver extends AbstractSymlinkResolver {

    private List<Path> files;

    public DownloadSymlinkResolver(final List<Path> files) {
        this.files = files;
    }

    @Override
    public boolean resolve(final Path file) {
        if(file.attributes().isSymbolicLink()) {
            if(Preferences.instance().getBoolean("path.symboliclink.resolve")) {
                // Resolve links instead
                return false;
            }
            // Create symbolic link only if choosen in the preferences. Otherwise download target file
            final Path target = file.getSymlinkTarget();
            // Only create symbolic link if target is included in the download
            for(Path root : files) {
                if(this.findTarget(target, root)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean include(final Path file) {
        if(file.attributes().isSymbolicLink()) {
            final Path target = file.getSymlinkTarget();
            // Do not transfer files referenced from symlinks pointing to files also included
            for(Path root : files) {
                if(this.findTarget(target, root)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findTarget(final Path target, final Path root) {
        return target.equals(root) || target.isChild(root);
    }
}