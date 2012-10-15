package ch.cyberduck.core.urlhandler;

import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.editor.Application;

import java.util.List;

/**
 * @version $Id:$
 */
public abstract class AbstractSchemeHandler implements SchemeHandler {

    /**
     * Register this bundle identifier as the default application for all schemes
     *
     * @param schemes     The protocol identifier
     * @param application The bundle identifier of the application
     */
    @Override
    public void setDefaultHandler(final List<Scheme> schemes, final Application application) {
        for(Scheme scheme : schemes) {
            this.setDefaultHandlerForScheme(application, scheme);
        }
    }

    public abstract void setDefaultHandlerForScheme(Application application, Scheme scheme);

    /**
     * @param schemes The protocol identifier
     * @return True if this application is the default handler for all schemes
     */
    @Override
    public boolean isDefaultHandler(final List<Scheme> schemes, final Application application) {
        boolean isDefault = true;
        for(Scheme s : schemes) {
            if(!application.equals(this.getDefaultHandler(s))) {
                isDefault = false;
                break;
            }
        }
        return isDefault;
    }
}