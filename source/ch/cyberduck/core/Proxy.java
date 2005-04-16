package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class Proxy {
	private static Logger log = Logger.getLogger(Proxy.class);

	static {
		try {
			NSBundle bundle = NSBundle.mainBundle();
			String lib = bundle.resourcePath()+"/Java/"+"libProxy.jnilib";
			System.load(lib);
		}
		catch(UnsatisfiedLinkError e) {
			log.error("Could not load the proxy library:"+e.getMessage());
		}
	}

	public static native boolean isSOCKSProxyEnabled();

	public static native String getSOCKSProxyHost();

	public static native int getSOCKSProxyPort();

	public static boolean isSOCKSAuthenticationEnabled() {
		Login l = new Login(new Host("socks", getSOCKSProxyHost(), getSOCKSProxyPort()),
		    getSOCKSProxyUser(), null);
		return l.getInternetPasswordFromKeychain() != null;
	}

	public static native String getSOCKSProxyUser();

	public static String getSOCKSProxyPassword() {
		Login l = new Login(new Host("socks", getSOCKSProxyHost(), getSOCKSProxyPort()),
		    getSOCKSProxyUser(), null);
		return l.getInternetPasswordFromKeychain();
	};
}