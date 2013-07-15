package ch.cyberduck.core;

import ch.cyberduck.core.local.Local;

/**
 * @version $Id$
 */
public class NullLocal extends Local {

    public NullLocal(final String parent, final String name) {
        super(parent + "/" + name);
    }

    @Override
    public boolean exists() {
        return true;
    }
}
