package ch.cyberduck.core.util;

import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * A wrapper for the handler functions in ApplicationServices.h
 * @version $Id$
 */
public class URLSchemeHandlerConfiguration {
    private static Logger log = Logger.getLogger(URLSchemeHandlerConfiguration.class);

    private static URLSchemeHandlerConfiguration instance;

    public static URLSchemeHandlerConfiguration instance() {
        if(null == instance) {
            instance = new URLSchemeHandlerConfiguration();
        }
        return instance;
    }

    private URLSchemeHandlerConfiguration() {
        ;
    }

    static {
        // Ensure native keychain library is loaded
        try {
            NSBundle bundle = NSBundle.mainBundle();
            String lib = bundle.resourcePath() + "/Java/" + "libURLSchemeHandlerConfiguration.dylib";
            log.info("Locating libURLSchemeHandlerConfiguration.dylib at '" + lib + "'");
            System.load(lib);
            log.info("libURLSchemeHandlerConfiguration.dylib loaded");
        }
        catch (UnsatisfiedLinkError e) {
            log.error("Could not load the libURLSchemeHandlerConfiguration.dylib library:" + e.getMessage());
            throw e;
        }
    }

    /**
     * See ApplicationServices/ApplicationServices.h#LSSetDefaultHandlerForURLScheme
     * Register this bundle identifier as the default application for all schemes
     * @param scheme The protocol identifier
     * @param bundleIdentifier The bundle identifier of the application
     */
    public native void setDefaultHandlerForURLScheme(String scheme, String bundleIdentifier);

    /**
     * Register this bundle identifier as the default application for all schemes
     * @param scheme The protocol identifier
     * @param bundleIdentifier The bundle identifier of the application
     */
    public void setDefaultHandlerForURLScheme(String[] scheme, String bundleIdentifier) {
        for(int i = 0; i < scheme.length; i++) {
            this.setDefaultHandlerForURLScheme(scheme[i], bundleIdentifier);
        }
    }

    /**
     * See ApplicationServices/ApplicationServices.h#LSCopyDefaultHandlerForURLScheme
     * @param scheme The protocol identifier
     * @return The bundle identifier for the application registered as the default handler for this scheme
     */
    public native String getDefaultHandlerForURLScheme(String scheme);

    /**
     * See ApplicationServices/ApplicationServices.h#LSCopyAllHandlersForURLScheme
     * @param scheme The protocol identifier
     * @return The bundle identifiers for all applications that promise to be capable of handling this scheme
     */
    public native String[] getAllHandlersForURLScheme(String scheme);

    /**
     * @param scheme The protocol identifier
     * @return True if this application is the default handler for the scheme
     */
    public boolean isDefaultHandlerForURLScheme(String scheme) {
        return NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleIdentifier").toString().equals(
                this.getDefaultHandlerForURLScheme(scheme)
        );
    }

    /**
     * @param scheme The protocol identifier
     * @return True if this application is the default handler for all schemes
     */
    public boolean isDefaultHandlerForURLScheme(String[] scheme) {
        boolean isDefault = true;
        for(int i = 0; i < scheme.length; i++) {
            if(!this.isDefaultHandlerForURLScheme(scheme[i])) {
                isDefault = false;
                break;
            }
        }
        return isDefault;
    }
}
