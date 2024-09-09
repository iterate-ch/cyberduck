package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

public class DelegatingLicenseVerifierCallback implements LicenseVerifierCallback {

    private final LicenseVerifierCallback[] proxies;

    public DelegatingLicenseVerifierCallback(final LicenseVerifierCallback... proxies) {
        this.proxies = proxies;
    }

    @Override
    public void failure(final InvalidLicenseException failure) {
        for(LicenseVerifierCallback proxy : proxies) {
            proxy.failure(failure);
        }
    }
}
