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
package com.sshtools.j2ssh.openssh;

import com.sshtools.j2ssh.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author $author$
 * @version $Revision$
 */
public class PEMReader extends PEM {
	private LineNumberReader reader;
	private String type;
	private Map header;
	private byte[] payload;

	/**
	 * Creates a new PEMReader object.
	 *
	 * @param r
	 * @throws IOException
	 */
	public PEMReader(Reader r) throws IOException {
		reader = new LineNumberReader(r);
		read();
	}

	private void read() throws IOException {
		String line;

		while((line = reader.readLine()) != null) {
			if(line.startsWith(PEM_BOUNDARY) && line.endsWith(PEM_BOUNDARY)) {
				if(line.startsWith(PEM_BEGIN)) {
					type = line.substring(PEM_BEGIN.length(),
					    line.length()-PEM_BOUNDARY.length());

					break;
				}
				else {
					throw new IOException("Invalid PEM boundary at line "+
					    reader.getLineNumber()+": "+line);
				}
			}
		}

		header = new HashMap();

		while((line = reader.readLine()) != null) {
			int colon = line.indexOf(':');

			if(colon == -1) {
				break;
			}

			String key = line.substring(0, colon).trim();

			if(line.endsWith("\\")) {
				String v = line.substring(colon+1, line.length()-1).trim();

				// multi-line value
				StringBuffer value = new StringBuffer(v);

				while((line = reader.readLine()) != null) {
					if(line.endsWith("\\")) {
						value.append(" ").append(line.substring(0,
						    line.length()-1).trim());
					}
					else {
						value.append(" ").append(line.trim());

						break;
					}
				}
			}
			else {
				String value = line.substring(colon+1).trim();
				header.put(key, value);
			}
		}

		// first line that is not part of the header
		// could be an empty line, but if there is no header and the body begins straight after the -----
		// then this line contains data
		if(line == null) {
			throw new IOException("The key format is invalid! OpenSSH formatted keys must begin with -----BEGIN RSA or -----BEGIN DSA");
		}

		StringBuffer body = new StringBuffer(line);

		while((line = reader.readLine()) != null) {
			if(line.startsWith(PEM_BOUNDARY) && line.endsWith(PEM_BOUNDARY)) {
				if(line.startsWith(PEM_END+type)) {
					break;
				}
				else {
					throw new IOException("Invalid PEM end boundary at line "+
					    reader.getLineNumber()+": "+line);
				}
			}

			body.append(line);
		}

		payload = Base64.decode(body.toString());
	}

	/**
	 * @return
	 */
	public Map getHeader() {
		return header;
	}

	/**
	 * @return
	 */
	public byte[] getPayload() {
		return payload;
	}

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param passphrase
	 * @return
	 * @throws GeneralSecurityException
	 * @throws NoSuchAlgorithmException
	 */
	public byte[] decryptPayload(String passphrase)
	    throws GeneralSecurityException {
		String dekInfo = (String)header.get("DEK-Info");

		if(dekInfo != null) {
			int comma = dekInfo.indexOf(',');
			String keyAlgorithm = dekInfo.substring(0, comma);

			if(!"DES-EDE3-CBC".equals(keyAlgorithm)) {
				throw new NoSuchAlgorithmException("Unsupported passphrase algorithm: "+keyAlgorithm);
			}

			String ivString = dekInfo.substring(comma+1);
			byte[] iv = new byte[ivString.length()/2];

			for(int i = 0; i < ivString.length(); i += 2) {
				iv[i/2] = (byte)Integer.parseInt(ivString.substring(i, i+
				    2), 16);
			}

			Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
			SecretKey key = getKeyFromPassphrase(passphrase, iv, 24);
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

			byte[] plain = new byte[payload.length];
			cipher.update(payload, 0, payload.length, plain, 0);

			return plain;
		}
		else {
			return payload;
		}
	}
}
