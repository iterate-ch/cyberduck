/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.transport.cipher;

import org.apache.log4j.Logger;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.sshtools.j2ssh.transport.AlgorithmOperationException;


/**
 * Implements the blowfish-cbc encryption algorithm for j2ssh
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class BlowfishCbc
    extends SshCipher {
    private static Logger log = Logger.getLogger(BlowfishCbc.class);

    /** The blowfish algorithm name for SSH */
    protected static String algorithmName = "blowfish-cbc";
    Cipher cipher;

    /**
     * Constructor for the BlowfishCbc object
     */
    public BlowfishCbc() {
    }

    /**
     * Gets the cipher block size
     *
     * @return The blockSize value
     */
    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    /**
     * Called to initate the cipher.
     *
     * @param mode ENCRYPT_MODE or DECRYPT_MODE
     * @param iv The initialization vector data
     * @param keydata The key data
     *
     * @exception AlgorithmOperationException if a provider cannot be found for
     *            the cipher
     */
    public void init(int mode, byte iv[], byte keydata[])
              throws AlgorithmOperationException {
        try {
            cipher = Cipher.getInstance("Blowfish/CBC/NoPadding");

            // Create a 16 byte key
            byte actualKey[] = new byte[16];
            System.arraycopy(keydata, 0, actualKey, 0, actualKey.length);

            SecretKeySpec keyspec = new SecretKeySpec(actualKey, "Blowfish");

            // Create the cipher according to its algorithm
            cipher.init(((mode==ENCRYPT_MODE) ? Cipher.ENCRYPT_MODE
                                              : Cipher.DECRYPT_MODE), keyspec,
                        new IvParameterSpec(iv, 0, cipher.getBlockSize()));
        } catch (NoSuchPaddingException nspe) {
            log.error("Blowfish initialization failed", nspe);
            throw new AlgorithmOperationException("No Padding not supported");
        } catch (NoSuchAlgorithmException nsae) {
            log.error("Blowfish initialization failed", nsae);
            throw new AlgorithmOperationException("Algorithm not supported");
        } catch (InvalidKeyException ike) {
            log.error("Blowfish initialization failed", ike);
            throw new AlgorithmOperationException("Invalid encryption key");

            /*} catch (InvalidKeySpecException ispe) {
                throw new AlgorithmOperationException("Invalid encryption key specification");*/
        } catch (InvalidAlgorithmParameterException ape) {
            log.error("Blowfish initialization failed", ape);
            throw new AlgorithmOperationException("Invalid algorithm parameter");
        }
    }

    /**
     * Transforms the data according to the cipher mode.
     *
     * @param data The untransformed data
     * @param offset The offset to start from in the untransformed data
     * @param len The length of data to transform
     *
     * @return The transformed data
     *
     * @exception AlgorithmOperationException if an algorithm operation fails
     */
    public byte[] transform(byte data[], int offset, int len)
                     throws AlgorithmOperationException {
        return cipher.update(data, offset, len);
    }
}
