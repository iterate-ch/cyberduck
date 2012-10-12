package ch.cyberduck.core.editor;

import ch.cyberduck.core.Local;

import java.util.List;

/**
 * @version $Id$
 */
public interface ApplicationFinder {

    /**
     * @return All of the application bundle identifiers that are capable of handling
     *         the specified content type in the specified roles.
     */
    List<String> findAll(Local file);

    /**
     * Find application for file type.
     *
     * @param file File
     * @return Absolute path to installed application
     */
    String find(Local file);

    /**
     * Determine the human readable application name for a given bundle identifier.
     *
     * @param application Bundle identifier
     * @return Application human readable name
     */
    String getName(String application);

    /**
     * @param application Bundle identifier
     * @return True if the application is launched
     */
    boolean isOpen(String application);
}
