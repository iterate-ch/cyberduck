package ch.cyberduck.core.local;

/**
 * @version $Id:$
 */
public interface RevealService {
    /**
     * Reveal file in file browser
     *
     * @param file File or folder
     */
    void reveal(Local file);
}
