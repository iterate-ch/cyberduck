/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.transport.publickey;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.io.ByteArrayReader;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshPublicKeyFile {
    private static Log log = LogFactory.getLog(SshPublicKeyFile.class);
    private SshPublicKeyFormat format;
    private byte[] keyblob;
    private String comment;

    /**
     * Creates a new SshPublicKeyFile object.
     *
     * @param keyblob
     * @param format
     */
    protected SshPublicKeyFile(byte[] keyblob, SshPublicKeyFormat format) {
        this.keyblob = keyblob;
        this.format = format;
    }

    /**
     * @return
     */
    public byte[] getBytes() {
        return format.formatKey(keyblob);
    }

    /**
     * @return
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return
     */
    public byte[] getKeyBlob() {
        return keyblob;
    }

    /**
     * @param key
     * @param format
     * @return
     */
    public static SshPublicKeyFile create(SshPublicKey key,
                                          SshPublicKeyFormat format) {
        SshPublicKeyFile file = new SshPublicKeyFile(key.getEncoded(), format);
        file.setComment(format.getComment());

        return file;
    }

    /**
     * @param keyfile
     * @return
     * @throws InvalidSshKeyException
     * @throws IOException
     */
    public static SshPublicKeyFile parse(File keyfile)
            throws InvalidSshKeyException, IOException {
        FileInputStream in = new FileInputStream(keyfile);
        byte[] data = new byte[in.available()];
        in.read(data);
        in.close();

        return parse(data);
    }

    /**
     * @param formattedKey
     * @return
     * @throws InvalidSshKeyException
     */
    public static SshPublicKeyFile parse(byte[] formattedKey)
            throws InvalidSshKeyException {
        log.info("Parsing public key file");

        // Try the default private key format
        SshPublicKeyFormat format;
        format = SshPublicKeyFormatFactory.newInstance(SshPublicKeyFormatFactory.getDefaultFormatType());

        boolean valid = format.isFormatted(formattedKey);

        if (!valid) {
            log.info("Public key is not in the default format, attempting parse with other supported formats");

            Iterator it = SshPublicKeyFormatFactory.getSupportedFormats()
                    .iterator();
            String ft;

            while (it.hasNext() && !valid) {
                ft = (String)it.next();
                log.debug("Attempting " + ft);
                format = SshPublicKeyFormatFactory.newInstance(ft);
                valid = format.isFormatted(formattedKey);
            }
        }

        if (valid) {
            SshPublicKeyFile file = new SshPublicKeyFile(format.getKeyBlob(formattedKey), format);
            file.setComment(format.getComment());

            return file;
        }
        else {
            throw new InvalidSshKeyException("The key format is not a supported format");
        }
    }

    /**
     * @return
     */
    public String getAlgorithm() {
        return ByteArrayReader.readString(keyblob, 0);
    }

    /**
     * @param newFormat
     * @throws InvalidSshKeyException
     */
    public void setFormat(SshPublicKeyFormat newFormat)
            throws InvalidSshKeyException {
        if (newFormat.supportsAlgorithm(getAlgorithm())) {
            newFormat.setComment(format.getComment());
            this.format = newFormat;
        }
        else {
            throw new InvalidSshKeyException("The format does not support the public key algorithm");
        }
    }

    /**
     * @return
     */
    public SshPublicKeyFormat getFormat() {
        return format;
    }

    /**
     * @return
     * @throws IOException
     */
    public SshPublicKey toPublicKey() throws IOException {
        ByteArrayReader bar = new ByteArrayReader(keyblob);
        String type = bar.readString();
        SshKeyPair pair = SshKeyPairFactory.newInstance(type);

        return pair.decodePublicKey(keyblob);
    }

    /**
     * @return
     */
    public String toString() {
        return new String(format.formatKey(keyblob));
    }
}
