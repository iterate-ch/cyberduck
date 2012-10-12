package ch.cyberduck.core.local;

import ch.cyberduck.core.Local;

/**
 * @version $Id:$
 */
public interface IconService {

    /**
     * @param file  File
     * @param image Image name
     * @return True if icon is set
     */
    boolean setIcon(Local file, String image);

    /**
     * @param file     File
     * @param progress Progress between -1 to 9
     * @return True if icon is set
     */
    boolean setProgress(Local file, int progress);
}
