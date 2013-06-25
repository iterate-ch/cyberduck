package ch.cyberduck.core;

import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public final class PathRelativizer {

    private PathRelativizer() {
        //
    }

    public static String relativize(final String root, String path) {
        if(StringUtils.contains(path, root)) {
            return path.substring(path.indexOf(root) + root.length());
        }
        return path;
    }
}
