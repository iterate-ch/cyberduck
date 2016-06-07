package ch.cyberduck.core.idna;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.IDN;

public class PunycodeConverter {
    private static final Logger log = Logger.getLogger(PunycodeConverter.class);

    /**
     * @return IDN normalized hostname
     */
    public String convert(final String hostname) {
        if(!PreferencesFactory.get().getBoolean("connection.hostname.idn")) {
            return StringUtils.strip(hostname);
        }
        if(StringUtils.isNotEmpty(hostname)) {
            try {
                // Convenience function that implements the IDNToASCII operation as defined in
                // the IDNA RFC. This operation is done on complete domain names, e.g: "www.example.com".
                // It is important to note that this operation can fail. If it fails, then the input
                // domain name cannot be used as an Internationalized Domain Name and the application
                // should have methods defined to deal with the failure.
                // IDNA.DEFAULT Use default options, i.e., do not process unassigned code points
                // and do not use STD3 ASCII rules If unassigned code points are found
                // the operation fails with ParseException
                final String idn = IDN.toASCII(StringUtils.strip(hostname));
                if(log.isDebugEnabled()) {
                    if(!StringUtils.equals(StringUtils.strip(hostname), idn)) {
                        log.debug(String.format("IDN hostname for %s is %s", hostname, idn));
                    }
                }
                if(StringUtils.isNotEmpty(idn)) {
                    return idn;
                }
            }
            catch(IllegalArgumentException e) {
                log.warn(String.format("Failed to convert hostname %s to IDNA", hostname), e);
            }
        }
        return StringUtils.strip(hostname);
    }
}
