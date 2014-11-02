package ch.cyberduck.core.io;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.ChecksumException;

import org.apache.commons.codec.binary.Hex;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @version $Id$
 */
public class SHA256ChecksumCompute implements ChecksumCompute {

    @Override
    public String compute(final InputStream in) throws ChecksumException {
        try {
            return Hex.encodeHexString(ServiceUtils.hashSHA256(in));
        }
        catch(IOException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e.getMessage(), e);
        }
    }
}
