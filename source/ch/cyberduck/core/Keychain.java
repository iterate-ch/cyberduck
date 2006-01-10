package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class Keychain {
    private static Logger log = Logger.getLogger(Keychain.class);

    private static Keychain instance;

    private Keychain() {
        //
    }

    public static Keychain instance() {
        if (null == instance) {
            instance = new Keychain();
        }
        return instance;
    }

    static {
        // Ensure native keychain library is loaded
        try {
            NSBundle bundle = NSBundle.mainBundle();
            String lib = bundle.resourcePath() + "/Java/" + "libKeychain.dylib";
            log.info("Locating libKeychain.dylib at '" + lib + "'");
            System.load(lib);
        }
        catch (UnsatisfiedLinkError e) {
            log.error("Could not load the libKeychain.dylib library:" + e.getMessage());
        }
    }

    /**
     *
     * @param protocol
     * @param serviceName
     * @param user
     * @return
     */
    public native String getInternetPasswordFromKeychain(String protocol, String serviceName, String user);

    /**
     *
     * @param serviceName
     * @param user
     * @return
     */
    public native String getPasswordFromKeychain(String serviceName, String user);

    /**
     *
     * @param serviceName
     * @param user
     * @param password
     */
    public native void addPasswordToKeychain(String serviceName, String user, String password);

    /**
     *
     * @param protocol
     * @param serviceName
     * @param user
     * @param password
     */
    public native void addInternetPasswordToKeychain(String protocol, String serviceName, String user, String password);

    /**
     *
     * @param certificate
     * @return
     */
    public native boolean hasCertificate(byte[] certificate);

    /**
     *
     * @param certificate
     */
    public native void addCertificateToKeychain(byte[] certificate);
}
