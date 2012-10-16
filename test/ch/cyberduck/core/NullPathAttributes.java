package ch.cyberduck.core;

/**
 * @version $Id$
 */
public class NullPathAttributes extends PathAttributes {

    public NullPathAttributes() {
        super(Path.FILE_TYPE);
    }

    public NullPathAttributes(int filetype) {
        super(filetype);
    }
}
