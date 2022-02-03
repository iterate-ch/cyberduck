package ch.cyberduck.core.i18n;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class BundleRegexLocale extends BundleLocale {
    private static final Logger log = LogManager.getLogger(BundleRegexLocale.class.getName());

    /**
     * For lookup in *.strings.1
     */
    private final RegexLocale fallback = new RegexLocale();

    public BundleRegexLocale() {
        final List<String> languages = PreferencesFactory.get().getList("AppleLanguages");
        if(!languages.isEmpty()) {
            try {
                fallback.setDefault(
                        LocaleUtils.toLocale(StringUtils.replace(languages.iterator().next(), "-", "_")).getLanguage());

            }
            catch(IllegalArgumentException e) {
                log.warn(String.format("Failure to parse default language set. %s", e.getMessage()));
            }
        }
    }

    public void setDefault(final String language) {
        fallback.setDefault(language);
        super.setDefault(language);
    }

    @Override
    public String localize(final String key, final String table) {
        final String localized = super.localize(key, table);
        if(StringUtils.equals(localized, key)) {
            return fallback.localize(key, table);
        }
        return localized;
    }
}
