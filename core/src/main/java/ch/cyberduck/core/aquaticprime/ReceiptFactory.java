package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.util.List;

public class ReceiptFactory extends LicenseFactory {
    private static final Logger log = Logger.getLogger(ReceiptFactory.class);

    /**
     * Application has determined that its receipt is invalid. Exit with a status of 173
     */
    private static final int APPSTORE_VALIDATION_FAILURE = 173;

    public ReceiptFactory() {
        super(LocalFactory.get(PreferencesFactory.get().getProperty("application.receipt.path")),
                new Filter<Local>() {
                    @Override
                    public boolean accept(final Local file) {
                        return "receipt".equals(file.getName());
                    }
                });
    }

    public ReceiptFactory(final Local folder) {
        super(folder, new Filter<Local>() {
            @Override
            public boolean accept(final Local file) {
                return "receipt".equals(file.getName());
            }
        });
    }

    @Override
    protected License create() {
        return new DefaultLicenseFactory(this).create();
    }

    @Override
    protected License open(final Local file) {
        // Verify immediately and exit if not a valid receipt
        final ReceiptVerifier verifier = new ReceiptVerifier(file);
        if(verifier.verify()) {
            // Set name
            final Receipt receipt = new Receipt(file, verifier.getGuid());
            if(log.isInfoEnabled()) {
                log.info(String.format("Valid receipt %s in %s", receipt, file));
            }
            // Copy to Application Support for users switching versions
            final Local support = LocalFactory.get(
                    PreferencesFactory.get().getProperty("application.support.path"));
            try {
                file.copy(LocalFactory.get(support, String.format("%s.cyberduckreceipt", receipt.getName())));
            }
            catch(AccessDeniedException e) {
                log.warn(e.getMessage());
            }
            return receipt;
        }
        else {
            log.error(String.format("Invalid receipt found in %s", file));
            System.exit(APPSTORE_VALIDATION_FAILURE);
        }
        return null;
    }

    @Override
    public List<License> open() throws AccessDeniedException {
        final List<License> keys = super.open();
        if(keys.isEmpty()) {
            System.exit(APPSTORE_VALIDATION_FAILURE);
        }
        return keys;
    }
}
