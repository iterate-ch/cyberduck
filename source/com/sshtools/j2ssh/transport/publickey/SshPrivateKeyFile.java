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

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshPrivateKeyFile {
	private static Log log = LogFactory.getLog(SshPrivateKeyFile.class);
	private SshPrivateKeyFormat format;
	private byte[] keyblob;

	/**
	 * Creates a new SshPrivateKeyFile object.
	 *
	 * @param keyblob
	 * @param format
	 */
	protected SshPrivateKeyFile(byte[] keyblob, SshPrivateKeyFormat format) {
		this.keyblob = keyblob;
		this.format = format;
	}

	/**
	 * @return
	 */
	public byte[] getBytes() {
		return keyblob;
	}

	/**
	 * @param passphrase
	 * @return
	 * @throws InvalidSshKeyException
	 */
	public byte[] getKeyBlob(String passphrase) throws InvalidSshKeyException {
		return format.decryptKeyblob(keyblob, passphrase);
	}

	/**
	 * @param oldPassphrase
	 * @param newPassphrase
	 * @throws InvalidSshKeyException
	 */
	public void changePassphrase(String oldPassphrase, String newPassphrase)
	    throws InvalidSshKeyException {
		byte[] raw = format.decryptKeyblob(keyblob, oldPassphrase);
		keyblob = format.encryptKeyblob(raw, newPassphrase);
	}

	/**
	 * @param formattedKey
	 * @return
	 * @throws InvalidSshKeyException
	 */
	public static SshPrivateKeyFile parse(byte[] formattedKey)
	    throws InvalidSshKeyException {
		if(formattedKey == null) {
			throw new InvalidSshKeyException("Key data is null");
		}

		log.info("Parsing private key file");

		// Try the default private key format
		SshPrivateKeyFormat format;
		format = SshPrivateKeyFormatFactory.newInstance(SshPrivateKeyFormatFactory.getDefaultFormatType());

		boolean valid = format.isFormatted(formattedKey);

		if(!valid) {
			log.info("Private key is not in the default format, attempting parse with other supported formats");

			Iterator it = SshPrivateKeyFormatFactory.getSupportedFormats()
			    .iterator();
			String ft;

			while(it.hasNext() && !valid) {
				ft = (String)it.next();
				log.debug("Attempting "+ft);
				format = SshPrivateKeyFormatFactory.newInstance(ft);
				valid = format.isFormatted(formattedKey);
			}
		}

		if(valid) {
			return new SshPrivateKeyFile(formattedKey, format);
		}
		else {
			throw new InvalidSshKeyException("The key format is not a supported format");
		}
	}

	/**
	 * @param keyfile
	 * @return
	 * @throws InvalidSshKeyException
	 * @throws IOException
	 */
	public static SshPrivateKeyFile parse(File keyfile)
	    throws InvalidSshKeyException, IOException {
		FileInputStream in = new FileInputStream(keyfile);
		byte[] data = null;

		try {
			data = new byte[in.available()];
			in.read(data);
		}
		finally {
			try {
				if(in != null) {
					in.close();
				}
			}
			catch(IOException ex) {
			}
		}

		return parse(data);
	}

	/**
	 * @return
	 */
	public boolean isPassphraseProtected() {
		return format.isPassphraseProtected(keyblob);
	}

	/*public void changePassphrase(String oldPassphrase, String newPassphrase)
	 throws InvalidSshKeyException {
	 keyblob = format.changePassphrase(keyblob, oldPassphrase, newPassphrase);
	  }*/
	public static SshPrivateKeyFile create(SshPrivateKey key,
	                                       String passphrase, SshPrivateKeyFormat format)
	    throws InvalidSshKeyException {
		byte[] keyblob = format.encryptKeyblob(key.getEncoded(), passphrase);

		return new SshPrivateKeyFile(keyblob, format);
	}

	/**
	 * @param newFormat
	 * @param passphrase
	 * @throws InvalidSshKeyException
	 */
	public void setFormat(SshPrivateKeyFormat newFormat, String passphrase)
	    throws InvalidSshKeyException {
		byte[] raw = this.format.decryptKeyblob(keyblob, passphrase);
		format = newFormat;
		keyblob = format.encryptKeyblob(raw, passphrase);
	}

	/**
	 * @return
	 */
	public SshPrivateKeyFormat getFormat() {
		return format;
	}

	/**
	 * @param passphrase
	 * @return
	 * @throws InvalidSshKeyException
	 */
	public SshPrivateKey toPrivateKey(String passphrase)
	    throws InvalidSshKeyException {
		try {
			byte[] raw = format.decryptKeyblob(keyblob, passphrase);
			SshKeyPair pair = SshKeyPairFactory.newInstance(getAlgorithm(raw));

			return pair.decodePrivateKey(raw);
		}
		catch(AlgorithmNotSupportedException anse) {
			throw new InvalidSshKeyException("The public key algorithm for this private key is not supported");
		}
	}

	/**
	 * @return
	 */
	public String toString() {
		return new String(keyblob);
	}

	private String getAlgorithm(byte[] raw) {
		return ByteArrayReader.readString(raw, 0);
	}
}
