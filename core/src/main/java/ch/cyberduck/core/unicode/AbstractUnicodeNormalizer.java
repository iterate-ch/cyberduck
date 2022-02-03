package ch.cyberduck.core.unicode;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.Normalizer;

public abstract class AbstractUnicodeNormalizer implements UnicodeNormalizer {
    private static final Logger log = LogManager.getLogger(AbstractUnicodeNormalizer.class);

    private final Normalizer.Form form;

    public AbstractUnicodeNormalizer(final Normalizer.Form form) {
        this.form = form;
    }

    @Override
    public CharSequence normalize(final CharSequence name) {
        if(!Normalizer.isNormalized(name, form)) {
            // Canonical decomposition followed by canonical composition (default)
            final String normalized = Normalizer.normalize(name, form);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Normalized string %s to %s", name, normalized));
            }
            return normalized;
        }
        return name;
    }
}
