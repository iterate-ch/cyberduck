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
package com.sshtools.j2ssh.transport.cipher;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.io.IOUtil;
import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshCipherFactory {
	private static HashMap ciphers;
	private static String defaultCipher;
	private static Log log = LogFactory.getLog(SshCipherFactory.class);
	private static ArrayList supported;

	static {
		ciphers = new HashMap();

		log.info("Loading supported cipher algorithms");

		ciphers.put("3des-cbc", TripleDesCbc.class);
		ciphers.put("blowfish-cbc", BlowfishCbc.class);
		defaultCipher = "blowfish-cbc";

		try {
			Enumeration enum = ConfigurationLoader.getExtensionClassLoader()
			    .getResources("j2ssh.cipher");
			URL url;
			Properties properties = new Properties();
			InputStream in;

			while((enum != null) && enum.hasMoreElements()) {
				url = (URL)enum.nextElement();
				in = url.openStream();
				properties.load(in);
				IOUtil.closeStream(in);

				int num = 1;
				String name = "";
				Class cls;

				while(properties.getProperty("cipher.name."+
				    String.valueOf(num)) != null) {
					try {
						name = properties.getProperty("cipher.name."+
						    String.valueOf(num));
						cls = ConfigurationLoader.getExtensionClassLoader()
						    .loadClass(properties.getProperty("cipher.class."+String.valueOf(num)));
						cls.newInstance();
						ciphers.put(name, cls);
						log.info("Installed "+name+" cipher");
					}
					catch(Throwable ex) {
						log.info("Could not install cipher class for "+name,
						    ex);
					}

					num++;
				}
			}
		}
		catch(Throwable t) {
		}

		// Build a list of the supported ciphers
		supported = new ArrayList(ciphers.keySet());
	}

	/**
	 * Creates a new SshCipherFactory object.
	 */
	protected SshCipherFactory() {
	}

	/**
	 *
	 */
	public static void initialize() {
	}

	/**
	 * @return
	 */
	public static String getDefaultCipher() {
		return defaultCipher;
	}

	/**
	 * @return
	 */
	public static List getSupportedCiphers() {
		// Return the list
		return supported;
	}

	/**
	 * @param algorithmName
	 * @return
	 * @throws AlgorithmNotSupportedException
	 */
	public static SshCipher newInstance(String algorithmName)
	    throws AlgorithmNotSupportedException {
		log.info("Creating new "+algorithmName+" cipher instance");

		try {
			return (SshCipher)((Class)ciphers.get(algorithmName)).newInstance();
		}
		catch(Throwable t) {
			throw new AlgorithmNotSupportedException(algorithmName+
			    " is not supported!");
		}
	}
}
