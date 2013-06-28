package ch.cyberduck.core.transfer.symlink;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.local.Local;

import java.util.List;

/**
 * @version $Id$
 */
public class UploadSymlinkResolver extends AbstractSymlinkResolver {

    private List<Path> files;

    public UploadSymlinkResolver(final List<Path> files) {
        this.files = files;
    }

    @Override
    public boolean resolve(final Path file) {
        final Local local = file.getLocal();
        if(local.attributes().isSymbolicLink()) {
            if(Preferences.instance().getBoolean("local.symboliclink.resolve")) {
                // Resolve links instead
                return false;
            }
            // Create symbolic link only if supported by the host
            if(file.getSession().isCreateSymlinkSupported()) {
                final Local target = local.getSymlinkTarget();
                // Only create symbolic link if target is included in the upload
                for(Path root : files) {
                    if(this.findTarget(target, root)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean include(final Path file) {
        final Local local = file.getLocal();
        if(local.attributes().isSymbolicLink()) {
            final Local target = local.getSymlinkTarget();
            // Do not transfer files referenced from symlinks pointing to files also included
            for(Path root : files) {
                if(this.findTarget(target, root)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findTarget(final Local target, final Path root) {
        return target.equals(root.getLocal()) || target.isChild(root.getLocal());
    }
}