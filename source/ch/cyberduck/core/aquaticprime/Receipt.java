package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.PKCS7SignedData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.security.Security;
import java.util.Collection;

/**
 * @version $Id:$
 */
public class Receipt extends AbstractLicense {
    private static Logger log = Logger.getLogger(Receipt.class);

    public static void register() {
        LicenseFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    /**
     *
     */
    private static final int APPSTORE_VALIDATION_FAILURE = 173;

    private static class Factory extends LicenseFactory {
        @Override
        protected License open(final Local file) {
            AbstractLicense l = new Receipt(file);
            // Verify immediatly and exit if not a valid receipt
            l.verify();
            return l;
        }

        @Override
        protected License create() {
            Local receipt = LocalFactory.createLocal(Preferences.instance().getProperty("application.receipt.path"));
            if(receipt.exists()) {
                final Collection<File> receipts = FileUtils.listFiles(
                        new File(receipt.getAbsolute()),
                        new NameFileFilter("receipt"), FalseFileFilter.FALSE);
                for(File key : receipts) {
                    return open(LocalFactory.createLocal(key));
                }
            }
            log.info("No receipt found");
            System.exit(APPSTORE_VALIDATION_FAILURE);
            return LicenseFactory.EMPTY_LICENSE;
        }
    }

    /**
     * @param file The license key file.
     */
    public Receipt(Local file) {
        super(file);
    }

    /**
     * Verifies the App Store Receipt
     *
     * @return
     */
    public boolean verify() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            PKCS7SignedData signature = new PKCS7SignedData(IOUtils.toByteArray(new FileInputStream(
                    this.getFile().getAbsolute()
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
        return Locale.localizedString("Unknown");
    }
}
