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
	private static Logger log = Logger.getLogger(Path.class);

	private Codec() {
	}

	public static String encode(String text) {
		return Codec.encode(text, Preferences.instance().getProperty("browser.encoding"));
	}

	public static String encode(String text, String encoding) {
		String encoded = text;
		try {
//			log.info("Assuminging remote encoding:"+encoding);
//		 String roundTrip = new String(text.getBytes("UTF-8"), "UTF-8");
			encoded = new String(text.getBytes(), encoding);
		}
		catch (java.io.UnsupportedEncodingException e) {
			log.error(e.getMessage());
		}
		finally {
			return encoded;
		}
	}

	public static String decode(String text) {
		//@todo
		return text;
	}
	
	
	/*
	 try {
		 byte[] utf8Bytes = original.getBytes("UTF8");
		 byte[] defaultBytes = original.getBytes();
		 
		 String roundTrip = new String(utf8Bytes, "UTF8");
		 System.out.println("roundTrip = " + roundTrip);
		 
		 System.out.println();
		 printBytes(utf8Bytes, "utf8Bytes");
		 System.out.println();
		 printBytes(defaultBytes, "defaultBytes");
	 } catch (UnsupportedEncodingException e) {
		 e.printStackTrace();
	 }
	 */
}