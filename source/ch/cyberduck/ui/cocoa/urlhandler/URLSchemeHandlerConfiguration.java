package ch.cyberduck.ui.cocoa.urlhandler;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Native;
import ch.cyberduck.ui.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * A wrapper for the handler functions in ApplicationServices.h
 *
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

    static {
        Native.load("URLSchemeHandlerConfiguration");
    }

    /**
     * See ApplicationServices/ApplicationServices.h#LSSetDefaultHandlerForURLScheme
     * Register this bundle identifier as the default application for all schemes
     *
     * @param scheme           The protocol identifier
     * @param bundleIdentifier The bundle identifier of the application
     */
    public native void setDefaultHandlerForURLScheme(String scheme, String bundleIdentifier);

    /**
     * Register this bundle identifier as the default application for all schemes
     *
     * @param scheme           The protocol identifier
     * @param bundleIdentifier The bundle identifier of the application
     */
    public void setDefaultHandlerForURLScheme(String[] scheme, String bundleIdentifier) {
        for(String aScheme : scheme) {
            this.setDefaultHandlerForURLScheme(aScheme, bundleIdentifier);
        }
    }

    /**
     * See ApplicationServices/ApplicationServices.h#LSCopyDefaultHandlerForURLScheme
     *
     * @param scheme The protocol identifier
     * @return The bundle identifier for the application registered as the default handler for this scheme
     */
    public native String getDefaultHandlerForURLScheme(String scheme);

    /**
     * See ApplicationServices/ApplicationServices.h#LSCopyAllHandlersForURLScheme
     *
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
        for(String aScheme : scheme) {
            if(!this.isDefaultHandlerForURLScheme(aScheme)) {
                isDefault = false;
                break;
            }
        }
        return isDefault;
    }
}
