package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class Receipt extends AbstractLicense {
    private static final Logger log = Logger.getLogger(Receipt.class);

    public static void register() {
        LicenseFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    /**
     * Application has determined that its receipt is invalid. Exit with a status of 173
     */
    private static final int APPSTORE_VALIDATION_FAILURE = 173;

    private static class Factory extends LicenseFactory {
        @Override
        protected License open(final Local file) {
            // Verify immediately and exit if not a valid receipt
            final ReceptVerifier verifier = new ReceptVerifier(file);
            if(verifier.verify()) {
                // Set name
                final Receipt receipt = new Receipt(file, verifier.getGuid());
                // Copy to Application Support for users switching versions
                final Local support = LocalFactory.createLocal(
                        Preferences.instance().getProperty("application.support.path"));
                file.copy(LocalFactory.createLocal(support, String.format("%s.cyberduckreceipt", receipt.getName())));
                return receipt;
            }
            else {
                System.exit(APPSTORE_VALIDATION_FAILURE);
            }
            return null;
        }

        @Override
        protected License open() {
            Local receipt = LocalFactory.createLocal(Preferences.instance().getProperty("application.receipt.path"));
            if(receipt.exists()) {
                for(Local key : receipt.list().filter(new Filter<Local>() {
                    @Override
                    public boolean accept(final Local file) {
                        return "receipt".equals(file.getName());
                    }
                })) {
                    return open(key);
                }
            }
            log.info("No receipt found");
            System.exit(APPSTORE_VALIDATION_FAILURE);
            return LicenseFactory.EMPTY_LICENSE;
        }

        @Override
        protected License create() {
            return this.open();
        }
    }

    private Local file;

    private String guid;

    /**
     * @param file The license key file.
     */
    public Receipt(final Local file, final String guid) {
        super(file);
        this.file = file;
        this.guid = guid;
    }

    /**
     * Verifies the App Store Receipt
     *
     * @return False if receipt validation failed.
     */
    @Override
    public boolean verify() {
        // Always return true to dismiss donation prompt.
        return true;
    }

    @Override
    public boolean isReceipt() {
        return true;
    }

    @Override
    public String getValue(final String property) {
        return LocaleFactory.localizedString("Unknown");
    }

    @Override
    public String getName() {
        return guid;
    }
}
