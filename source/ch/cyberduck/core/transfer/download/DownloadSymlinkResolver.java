package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.transfer.SymlinkResolver;

import java.util.List;

/**
 * @version $Id:$
 */
public class DownloadSymlinkResolver implements SymlinkResolver {

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
            final AbstractPath target = file.getSymlinkTarget();
            // Only create symbolic link if target is included in the download
            for(Path root : files) {
                if(target.isChild(root)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean include(final Path file) {
        if(file.attributes().isSymbolicLink()) {
            final AbstractPath target = file.getSymlinkTarget();
            // Do not transfer files referenced from symlinks pointing to files also included
            for(Path root : files) {
                if(target.isChild(root)) {
                    return false;
                }
            }
        }
        return true;
    }
}
