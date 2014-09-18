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
import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * @version $Id$
 */
public class DonationKeyFactory extends LicenseFactory {
    private static final Logger log = Logger.getLogger(DonationKeyFactory.class);

    public static void register() {
        LicenseFactory.addFactory(DonationKeyFactory.NATIVE_PLATFORM, new DonationKeyFactory());
    }

    @Override
    protected License open(final Local file) {
        return new Donation(file);
    }

    @Override
    protected List<License> open() throws AccessDeniedException {
        final List<License> keys = super.open();
        if(keys.isEmpty()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("No donation key found"));
            }
            // No key found. Look for receipt
            for(Local file : folder.list().filter(new Filter<Local>() {
                @Override
                public boolean accept(final Local file) {
                    return "cyberduckreceipt".equals(FilenameUtils.getExtension(file.getName()));
                }
            })) {
                final ReceiptVerifier verifier = new ReceiptVerifier(file);
                if(verifier.verify()) {
                    keys.add(new Receipt(file, verifier.getGuid()));
                }
            }
        }
        if(keys.isEmpty()) {
            return Arrays.asList(LicenseFactory.EMPTY_LICENSE);
        }
        return keys;
    }
}
