package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public class NullAttributes extends Attributes {

    @Override
    public int getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getModificationDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getCreationDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getAccessedDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Permission getPermission() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public String getChecksum() {
        throw new UnsupportedOperationException();
    }
}
