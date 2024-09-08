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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.preferences.ApplicationResourcesFinderFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class DonationKeyFactory extends LicenseFactory {
    private static final Logger log = LogManager.getLogger(DonationKeyFactory.class);

    @Override
    protected License create() {
        return new DefaultLicenseFactory(this).create();
    }

    @Override
    protected License open(final Local file) {
        return new DonationKey(file);
    }

    @Override
    public List<License> open() throws AccessDeniedException {
        final List<License> keys = super.open();
        if(keys.isEmpty()) {
            if(log.isInfoEnabled()) {
                log.info("No donation key found");
            }
            final Pattern pattern = Pattern.compile(".*\\.cyberduckreceipt");
            // No key found. Look for receipt in sandboxed application container
            for(Local file : SupportDirectoryFinderFactory.get().find().list().filter(new Filter<Local>() {
                @Override
                public boolean accept(final Local file) {
                    return "cyberduckreceipt".equals(Path.getExtension(file.getName()));
                }

                @Override
                public Pattern toPattern() {
                    return pattern;
                }
            })) {
                final Receipt receipt = new Receipt(file);
                if(receipt.verify(new DisabledLicenseVerifierCallback())) {
                    keys.add(receipt);
                }
            }
        }
        if(keys.isEmpty()) {
            final Local bundle = ApplicationResourcesFinderFactory.get().find();
            if(bundle.exists()) {
                for(Local key : bundle.list().filter(new LicenseFilter())) {
                    log.info(String.format("Add bundled registration key %s", key));
                    final License registration = this.open(key);
                    keys.add(registration);
                }
            }
        }
        if(keys.isEmpty()) {
            return Collections.singletonList(LicenseFactory.EMPTY_LICENSE);
        }
        return keys;
    }
}
