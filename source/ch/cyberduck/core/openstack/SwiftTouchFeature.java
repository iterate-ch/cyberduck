package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DefaultTouchFeature;
import ch.cyberduck.core.Path;

/**
 * @version $Id:$
 */
public class SwiftTouchFeature extends DefaultTouchFeature {

    public SwiftTouchFeature(final SwiftSession session) {
        super(session);
    }

    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a container.
        return !workdir.isRoot();
    }
}
