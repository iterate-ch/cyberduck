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
    List<Application> findAll(Local file);

    /**
     * Find application for file type.
     *
     * @param file File
     * @return Absolute path to installed application
     */
    Application find(Local file);

    boolean isInstalled(Application application);

    Application find(String application);

    /**
     * @param application Bundle identifier
     * @return True if the application is launched
     */
    boolean isOpen(Application application);
}
