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
package com.sshtools.j2ssh.transport.compression;

import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author $author$
 * @version $Revision$
 */
public class SshCompressionFactory {
	/**  */
	public final static String COMP_NONE = "none";
	private static String defaultAlgorithm;
	private static Map comps;
	private static Log log = LogFactory.getLog(SshCompressionFactory.class);

	static {
		comps = new HashMap();

		log.info("Loading compression methods");

		comps.put(COMP_NONE, "");
		comps.put("zlib", com.sshtools.ext.jzlib.ZLibCompression.class);

		defaultAlgorithm = "zlib";
	}

	/**
	 * Creates a new SshCompressionFactory object.
	 */
	protected SshCompressionFactory() {
	}

	/**
	 * @return
	 */
	public static String getDefaultCompression() {
		return defaultAlgorithm;
	}

	/**
	 * @return
	 */
	public static List getSupportedCompression() {
		return new ArrayList(comps.keySet());
	}

	/**
	 * @param algorithmName
	 * @return
	 * @throws AlgorithmNotSupportedException
	 */
	public static SshCompression newInstance(String algorithmName)
	    throws AlgorithmNotSupportedException {
		try {
			if(algorithmName.equals(COMP_NONE)) {
				return null;
			}
			else {
				return (SshCompression)((Class)comps.get(algorithmName)).newInstance();
			}
		}
		catch(Exception e) {
			throw new AlgorithmNotSupportedException(algorithmName+
			    " is not supported!");
		}
	}
}
