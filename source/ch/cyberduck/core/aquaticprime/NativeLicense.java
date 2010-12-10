package ch.cyberduck.core.aquaticprime;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Native;
import ch.cyberduck.core.Preferences;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.PKCS7SignedData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.security.Security;

/**
 * @version $Id$
 */
public class NativeLicense extends AbstractLicense {
    private static Logger log = Logger.getLogger(NativeLicense.class);

    public static void register() {
        LicenseFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends LicenseFactory {
        @Override
        protected License open(final Local file) {
            if(file.getName().endsWith(".cyberducklicense")) {
                return new NativeLicense(file);
            }
            AbstractLicense l = new AbstractLicense(file) {
                private static final int APPSTORE_VALIDATION_FAILURE = 173;

                /**
                 * Verifies the App Store Receipt
                 * @return
                 */
                public boolean verify() {
                    try {
                        Security.addProvider(new BouncyCastleProvider());
                        PKCS7SignedData signature = new PKCS7SignedData(IOUtils.toByteArray(new FileInputStream(
                                file.getAbsolute()
                        )));
                        signature.verify();
                        return true;
                    }
                    catch(Throwable e) {
                        log.error(e.getMessage());
                        // Shutdown if receipt is not valid
                        System.exit(APPSTORE_VALIDATION_FAILURE);
                    }
                    return false;
                }

                @Override
                public boolean isReceipt() {
                    return true;
                }

                public String getValue(String property) {
                    return StringUtils.EMPTY;
                }

                @Override
                public String getName() {
                    String id = Preferences.instance().getProperty("AppleID");
                    if(StringUtils.isNotBlank(id)) {
                        return id;
                    }
                    return "Mac App Store";
                }
            };
            // Verify immediatly and exit if not a valid receipt
            l.verify();
            return l;
        }

        /**
         * @throws UnsupportedOperationException
         * @see #create(ch.cyberduck.core.Local)
         */
        @Override
        protected License create() {
            throw new UnsupportedOperationException();
        }
    }

    private static boolean JNI_LOADED = false;

    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("Prime");
        }
        return JNI_LOADED;
    }

    /**
     * @param file The license key file.
     */
    public NativeLicense(Local file) {
        super(file);
    }

    /**
     * @return True if valid license key
     */
    public boolean verify() {
        if(!NativeLicense.loadNative()) {
            return false;
        }
        final boolean valid = this.verify(this.getFile().getAbsolute());
        if(valid) {
            log.info("Valid donation key:" + this.getFile().getAbsolute());
        }
        else {
            log.warn("Not a valid donation key:" + this.getFile().getAbsolute());
        }
        return valid;
    }

    private native boolean verify(String license);

    /**
     * @return
     */
    public String getValue(String property) {
        if(!NativeLicense.loadNative()) {
            return null;
        }
        return this.getValue(this.getFile().getAbsolute(), property);
    }

    private native String getValue(String license, String property);
}
