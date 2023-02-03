package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DisabledLicenseVerifierCallback implements LicenseVerifierCallback {
    private static final Logger log = LogManager.getLogger(DisabledLicenseVerifierCallback.class);

    @Override
    public void failure(final InvalidLicenseException failure) {
        log.warn(String.format("Failure verifying registration key. %s", failure));
    }
}
