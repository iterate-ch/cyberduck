package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;

import net.schmizz.sshj.common.Buffer;

public class SSHFingerprintGenerator {

    /**
     * @param key Public key
     * @return The fingerprint is the MD5 of the Base64-encoded public key
     */
    public String fingerprint(final PublicKey key) throws ChecksumException {
        return this.fingerprint(new ByteArrayInputStream(
                new Buffer.PlainBuffer().putPublicKey(key).getCompactData()));
    }

    /**
     * The fingerprint of a public key consists of the output of the MD5
     * message-digest algorithm [RFC-1321].  The input to the algorithm is
     * the public key blob as described in [SSH-TRANS].  The output of the
     * algorithm is presented to the user as a sequence of 16 octets printed
     * as hexadecimal with lowercase letters and separated by colons.
     * <p>
     * For example: "4b:69:6c:72:6f:79:20:77:61:73:20:68:65:72:65:21"
     *
     * @param in Public key blob
     * @return The fingerprint is the MD5 of the Base64-encoded public key
     */
    public String fingerprint(final InputStream in) throws ChecksumException {
        final String undelimited = ChecksumComputeFactory.get(HashAlgorithm.md5).compute(in).hash;
        final StringBuilder fp = new StringBuilder(undelimited.substring(0, 2));
        for(int i = 2; i <= undelimited.length() - 2; i += 2) {
            fp.append(":").append(undelimited.substring(i, i + 2));
        }
        return fp.toString();
    }
}
