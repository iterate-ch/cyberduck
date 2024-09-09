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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;

public class Receipt extends AbstractLicense {

    private final ReceiptVerifier verifier;

    /**
     * @param file The license key file.
     */
    public Receipt(final Local file) {
        super(file);
        this.verifier = new ReceiptVerifier(file);
    }

    /**
     * Verifies the App Store Receipt
     *
     * @return False if receipt validation failed.
     */
    @Override
    public boolean verify(final LicenseVerifierCallback callback) {
        return verifier.verify(new DelegatingLicenseVerifierCallback(callback, new ExitCodeLicenseVerifierCallback()));
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
    public String getEntitlement() {
        return LocaleFactory.localizedString("Unknown");
    }
}
