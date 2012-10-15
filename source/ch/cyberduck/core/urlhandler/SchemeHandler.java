package ch.cyberduck.core.urlhandler;

import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.editor.Application;

import java.util.List;

/**
 * @version $Id:$
 */
public interface SchemeHandler {

    /**
     * @param scheme      URL scheme
     * @param application Application to set as default handler
     */
    void setDefaultHandler(List<Scheme> scheme, Application application);

    /**
     * @param scheme URL scheme
     * @return Null if no handler is set
     */
    Application getDefaultHandler(Scheme scheme);

    /**
     * @param scheme URL scheme
     * @return True if current application is configured as protocol handler
     */
    boolean isDefaultHandler(List<Scheme> scheme, Application application);

    /**
     * @param scheme URL schemes
     * @return True if current application is configured as protocol handler for all schemes
     */
    List<Application> getAllHandlers(Scheme scheme);
}
