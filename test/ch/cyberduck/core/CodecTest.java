package ch.cyberduck.core;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import junit.framework.TestCase;

public class CodecTest extends TestCase {
	
	static {
		org.apache.log4j.BasicConfigurator.configure();
	}
	
	public CodecTest(String name) {
		super(name);
	}
	
	
	public void testEncoding() {
		try {
			String testString = "aÃàoÃàuÃàaÃàaÃàaÃà!eÃÄ&%cÃß"; //encoded
			{
                String decoded = Codec.decode(testString);
                String encoded = new String(Codec.encode(decoded));
				assertTrue(testString.equals(encoded));
			}
			{
                String decoded = Codec.decode(testString, "ISO-8859-1");
                String encoded = new String(Codec.encode(decoded, "ISO-8859-1"));
				assertTrue(testString.equals(encoded));
			}
		}
		catch(java.lang.UnsatisfiedLinkError e) {}
	}
}