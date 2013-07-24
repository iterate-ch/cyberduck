package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.shared.DefaultTouchFeature;

/**
 * @version $Id$
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
