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
package com.sshtools.j2ssh.transport.publickey;

/**
 * Defines a Private Key format for use with SshPrivateKeyFile
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public interface SshPrivateKeyFormat
    extends SshKeyFormatConversion {
    /**
     * Examines the keyblob to determine if the keyblob is encrypted with a
     * passphrase
     *
     * @param keyblob
     *
     * @return <tt>true</tt> if the key is passphrase protected otherwise
     *         <tt>false</tt>
     */
    public boolean isPassphraseProtected(byte keyblob[]);

    /**
     * Changes the passphrase of the encrypted key blob. This method should
     * typically unencrypt the keyblob and apply the new passphrase by
     * encrypting with the new passphrase
     *
     * @param keyblob The existing keyblob encrpyted by the old passphrase
     * @param oldPassphrase The old passphrase
     * @param newPassphrase The new passphrase
     *
     * @return The new keyblob encrypted with the new passphrase
     *
     * @throws InvalidSshKeyException if the key is invalid or passphrase
     *         incorrect
     */
    public byte[] changePassphrase(byte keyblob[], String oldPassphrase,
                                   String newPassphrase)
                            throws InvalidSshKeyException;

    /**
     * Decrypts the keyblob using the passphrase
     *
     * @param keyblob The encrypted key blob
     * @param passphrase The passphrase
     *
     * @return The algorithms encoded key blob
     *
     * @throws InvalidSshKeyException if the key is invalid or passphrase
     *         incorrect
     */
    public byte[] decryptKeyblob(byte keyblob[], String passphrase)
                          throws InvalidSshKeyException;

    /**
     * Encrypts the keyblob using the passphrase
     *
     * @param keyblob The algorithms encoded key blob
     * @param passphrase The passphrase
     *
     * @return The encrypted key blob
     *
     * @throws InvalidSshKeyException if the key is invalid
     */
    public byte[] encryptKeyblob(byte keyblob[], String passphrase)
                          throws InvalidSshKeyException;

    /**
     * Determines if the algorithm name is supported by the format
     *
     * @param algorithm the algorithm name
     *
     * @return <tt>true</tt> if the format supports the algorithm otherwise
     *         <tt>false</tt>
     */
    public boolean supportsAlgorithm(String algorithm);
}
