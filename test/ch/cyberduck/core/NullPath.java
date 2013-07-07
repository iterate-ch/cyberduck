package ch.cyberduck.core;

/**
 * @version $Id$
 */
public class NullPath extends Path {

    public NullPath(final String path, final int type) {
        super(path, type);
    }

    public NullPath(final Path parent, final String name, final int type) {
        super(parent, name, type);
    }

    @Override
    public Path getParent() {
        return new NullPath(Path.getParent(this.getAbsolute(), '/'), Path.DIRECTORY_TYPE);
    }
}
