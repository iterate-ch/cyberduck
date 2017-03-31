package ch.cyberduck.core.udt.qloudsonic;

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
import ch.cyberduck.core.aquaticprime.DonationKey;
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.FilenameUtils;

import java.util.regex.Pattern;

public class QloudsonicVoucherFinder extends LicenseFactory {

    public QloudsonicVoucherFinder() {
        super(LocalFactory.get(
                PreferencesFactory.get().getProperty("application.support.path")), new Filter<Local>() {
            @Override
            public boolean accept(final Local file) {
                return "qloudsonicvoucher".equals(FilenameUtils.getExtension(file.getName()));
            }

            @Override
            public Pattern toPattern() {
                return Pattern.compile("qloudsonicvoucher");
            }
        });
    }

    @Override
    protected License create() {
        return new DefaultLicenseFactory(this).create();
    }

    @Override
    protected License open(final Local file) {
        return new DonationKey(file);
    }
}
