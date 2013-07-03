package ch.cyberduck.core;

/**
 * @version $Id$
 */
public class NullAttributes extends Attributes {

    @Override
    public int getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getSize() {
        return -1L;
    }

    @Override
    public long getModificationDate() {
        return -1L;
    }

    @Override
    public long getCreationDate() {
        return -1L;
    }

    @Override
    public long getAccessedDate() {
        return -1L;
    }

    @Override
    public Permission getPermission() {
        return Permission.EMPTY;
    }

    @Override
    public boolean isDirectory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVolume() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public String getChecksum() {
        throw new UnsupportedOperationException();
    }
}
