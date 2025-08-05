package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.io.Checksum;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class IRODSChecksumUtils {

    private static final Logger log = LogManager.getLogger(IRODSChecksumUtils.class);

    public static Checksum toChecksum(String irodsChecksum) {
        if(StringUtils.isBlank(irodsChecksum)) {
            return Checksum.NONE;
        }

        int colon = irodsChecksum.indexOf(':');
        if(-1 == colon) {
            log.debug("no hash algorithm prefix found in iRODS checksum. ignoring checksum.");
            return Checksum.NONE;
        }

        if(colon + 1 >= irodsChecksum.length()) {
            log.debug("iRODS checksum may be corrupted. ignoring checksum.");
            return Checksum.NONE;
        }

        log.debug("checksum from iRODS server is [{}].", irodsChecksum);
        String checksum = irodsChecksum.substring(colon + 1);
        checksum = Hex.encodeHexString(Base64.decodeBase64(checksum));
        log.debug("base64-decoded, hex-encoded checksum is [{}].", checksum);
        return Checksum.parse(checksum);
    }
}
