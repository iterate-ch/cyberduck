package ch.cyberduck.core;

/**
 * @version $Id$
 */
public class NullLocal extends Local {

    public NullLocal(final String parent, final String name) {
        super(parent, name);
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public void setIcon(final int progress) {
        throw new UnsupportedOperationException();
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
    public void writeUnixPermission(final Permission perm, final boolean recursive) {
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

    @Override
    public void setQuarantine(final String originUrl, final String dataUrl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWhereFrom(final String dataUrl) {
        throw new UnsupportedOperationException();
    }
}
