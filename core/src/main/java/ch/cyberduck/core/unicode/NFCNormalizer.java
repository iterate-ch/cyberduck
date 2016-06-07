package ch.cyberduck.core.unicode;

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

import org.apache.log4j.Logger;

import java.text.Normalizer;

public class NFCNormalizer {
    private static final Logger log = Logger.getLogger(NFCNormalizer.class);

    public String normalize(final String name) {
        if(!Normalizer.isNormalized(name, Normalizer.Form.NFC)) {
            // Canonical decomposition followed by canonical composition (default)
            final String normalized = Normalizer.normalize(name, Normalizer.Form.NFC);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Normalized local path %s to %s", name, normalized));
            }
            return normalized;
        }
        return name;
    }
}
