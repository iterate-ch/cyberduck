package ch.cyberduck.core;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class Codec {
	private static Logger log = Logger.getLogger(Codec.class);

	private Codec() {
	}

	public static String decode(String text) {
		return Codec.decode(text, Preferences.instance().getProperty("browser.decoding"));
	}

	/**
	 * Constructs a new String by decoding the specified array of bytes using the specified charset.
	 * The length of the new String is a function of the charset, and hence may not be equal to the length of the byte array.
	 */
	public static String decode(String text, String encoding) {
		log.debug("decode:" + text + "," + encoding);
		String decoded = text;
		try {
			decoded = new String(text.getBytes(), encoding);
		}
		catch (java.io.UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}
		finally {
			log.debug("Decoded: " + decoded);
			return decoded;
		}
	}

	public static String encode(String text) {
		return Codec.encode(text, Preferences.instance().getProperty("browser.encoding"));
	}

	/**
	 * Encodes this String into a sequence of bytes using the named charset, storing the result into a new byte array.
	 */
	public static String encode(String text, String encoding) {
		log.debug("encode:" + text + "," + encoding);
		String encoded = text;
		try {
			encoded = new String(text.getBytes(encoding));
		}
		catch (java.io.UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}
		finally {
			log.debug("Encoded: " + encoded);
			return encoded;
		}
	}
}