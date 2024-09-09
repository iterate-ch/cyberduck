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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

public class ReceiptFactory extends LicenseFactory {
    private static final Logger log = LogManager.getLogger(ReceiptFactory.class);

    public ReceiptFactory() {
        super(LocalFactory.get(PreferencesFactory.get().getProperty("application.receipt.path")),
                new ReceiptFilter());
    }

    public ReceiptFactory(final Local folder) {
        super(folder, new ReceiptFilter());
    }

    @Override
    protected License create() {
        return new DefaultLicenseFactory(this).create();
    }

    @Override
    protected License open(final Local file) {
        return new Receipt(file);
    }

    @Override
    protected License unregistered() {
        return EMPTY_LICENSE;
    }

    public static final License EMPTY_LICENSE = new License() {
        @Override
        public boolean verify(final LicenseVerifierCallback callback) {
            return false;
        }

        @Override
        public String getValue(String property) {
            return null;
        }

        @Override
        public String getEntitlement() {
            return LocaleFactory.localizedString("Not a valid registration key", "License");
        }

        @Override
        public boolean isReceipt() {
            return true;
        }
    };

    private static class ReceiptFilter implements Filter<Local> {
        private final Pattern pattern = Pattern.compile("receipt");

        @Override
        public boolean accept(final Local file) {
            return "receipt".equals(file.getName());
        }

        @Override
        public Pattern toPattern() {
            return pattern;
        }
    }
}
