package ch.cyberduck.core;

import ch.cyberduck.core.local.Local;

/**
 * @version $Id$
 */
public class NullLocal extends Local {

    public NullLocal(final String parent, final String name) {
        super(parent, name);
    }

    @Override
    public void trash() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean reveal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeUnixPermission(final Permission permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean open() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bounce() {
        throw new UnsupportedOperationException();
    }
}
