package ch.cyberduck.core.io;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.ChecksumException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.jets3t.service.utils.ServiceUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import net.schmizz.sshj.common.Buffer;

/**
 * @version $Id$
 */
public class MD5ChecksumCompute implements ChecksumCompute {

    @Override
    public String compute(final InputStream in) throws ChecksumException {
        try {
            return Hex.encodeHexString(ServiceUtils.computeMD5Hash(in));
        }
        catch(NoSuchAlgorithmException | IOException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e.getMessage(), e);
        }
    }

    public String compute(final String in) throws ChecksumException {
        try {
            return this.compute(new ByteArrayInputStream(Hex.decodeHex(in.toCharArray())));
        }
        catch(DecoderException e) {
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e.getMessage(), e);
        }
    }

    public String fingerprint(final PublicKey key) throws ChecksumException {
        return this.fingerprint(new ByteArrayInputStream(
                new Buffer.PlainBuffer().putPublicKey(key).getCompactData()));
    }

    public String fingerprint(final InputStream in) throws ChecksumException {
        final String undelimited = this.compute(in);
        StringBuilder fp = new StringBuilder(undelimited.substring(0, 2));
        for(int i = 2; i <= undelimited.length() - 2; i += 2) {
            fp.append(":").append(undelimited.substring(i, i + 2));
        }
        return fp.toString();
    }
}
