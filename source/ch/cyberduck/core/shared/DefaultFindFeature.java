package ch.cyberduck.core.shared;

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

/**
 * @version $Id:$
 */
public class DefaultFindFeature implements Find {

    private Session session;

    public DefaultFindFeature(final Session session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            return session.list(file.getParent(), new DisabledListProgressListener()).contains(file.getReference());
        }
        catch(NotfoundException e) {
            return false;
        }
    }
}
