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

import java.util.Hashtable;

import org.apache.log4j.Logger;

/**
 * Holding all application preferences. Default values get overwritten when loading
 * the <code>PREFERENCES_FILE</code>.
 * Singleton class.
 * @version $Id$
 */
public abstract class Preferences {
	private static Logger log = Logger.getLogger(Preferences.class);

	private static Preferences current = null;
	private Hashtable defaults;

	//	System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", Preferences.instance().getProperty("http.wire.logging"));
	//	System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", Preferences.instance().getProperty("http.logging"));

	/**
	 * @return The singleton instance of me.
	 */
	public static Preferences instance() {
		if (null == current) {
			current = new ch.cyberduck.ui.cocoa.CDPreferencesImpl();
			current.setDefaults();
			current.load();
		}
		return current;
	}

	/**
	 *	@param property The name of the property to overwrite
	 * 	@param value The new vlaue
	 */
	public abstract void setProperty(String property, String value);

	/**
	 *	@param property The name of the property to overwrite
	 * 	@param v The new vlaue
	 */
	public abstract void setProperty(String property, boolean v);

	/**
	 *	@param property The name of the property to overwrite
	 * 	@param v The new vlaue
	 */
	public abstract void setProperty(String property, int v);

	/**
	 * setting the default prefs values
	 */
	public void setDefaults() {
		this.defaults = new Hashtable();

		defaults.put("logging", "WARN");
		defaults.put("uses", "0");
		defaults.put("donate", "true");
		defaults.put("mail", "mailto:dkocher@cyberduck.ch");
		defaults.put("website.donate" , "http://cyberduck.ch/donate/");
		defaults.put("website.update.xml", "http://update.cyberduck.ch/versionlist.xml");
		defaults.put("website.update", "http://cyberduck.ch/");
		defaults.put("website.home", "http://cyberduck.ch/");

		defaults.put("bookmarkDrawer.isOpen", "false");
		defaults.put("logDrawer.isOpen", "false");

		defaults.put("browser.columnIcon", "true");
		defaults.put("browser.columnFilename", "true");
		defaults.put("browser.columnSize", "true");
		defaults.put("browser.columnModification", "true");
		defaults.put("browser.columnOwner", "true");
		defaults.put("browser.columnPermissions", "false");
		defaults.put("browser.alternatingRows", "true");
		defaults.put("browser.verticalLines", "false");
		defaults.put("browser.horizontalLines", "true");
		defaults.put("browser.openByDefault", "true");
		defaults.put("browser.showHidden", "false");
		defaults.put("browser.charset.encoding", "UTF-8");

		defaults.put("history.size", "10");
		defaults.put("history.save", "true");

		defaults.put("favorites.save", "true");

		defaults.put("queue.openByDefault", "false");
		defaults.put("queue.save", "true");
		defaults.put("queue.removeItemWhenComplete", "false");
		defaults.put("queue.postProcessItemWhenComplete", "false");
		defaults.put("queue.download.folder", System.getProperty("user.home"));
		defaults.put("queue.download.duplicate", "ask");
		defaults.put("queue.upload.changePermissions", "true");

		defaults.put("connection.login.name", System.getProperty("user.name"));
		defaults.put("connection.login.useKeychain", "true");

		defaults.put("connection.buffer", "1024");
		defaults.put("connection.buffer.default", "1024");
		defaults.put("connection.port.default", "21");
		defaults.put("connection.protocol.default", "ftp");

		//ftp properties
		defaults.put("ftp.anonymous.name", "anonymous");
		defaults.put("ftp.anonymous.pass", "cyberduck@example.net");
		defaults.put("ftp.connectmode", "passive");
		defaults.put("ftp.transfermode", "binary");
		defaults.put("ftp.sendSystemCommand", "true");
		defaults.put("ftp.sendExtendedListCommand", "true");

		defaults.put("http.agent", "Cyberduck/2.1");
		defaults.put("http.acceptheader", "*/*");

		defaults.put("ssh.knownhosts", System.getProperty("user.home") + "/.ssh/known_hosts");

		defaults.put("ssh.CSEncryption", "Default"); //client -> server encryption cipher
		defaults.put("ssh.SCEncryption", "Default"); //server -> client encryption cipher

		defaults.put("ssh.CSAuthentication", "Default"); //client -> server message authentication
		defaults.put("ssh.SCAuthentication", "Default"); //server -> client message authentication

		defaults.put("ssh.publickey", "Default");

		defaults.put("ssh.compression", "zlib");
	}


	/**
	 * Should be overriden by the implementation and only called if the property
	 * can't be found in the users's defaults table
	 * @param property The property to query.
	 *	@return The value of the property
	 */
	public String getProperty(String property) {
		String value = (String) defaults.get(property);
		if (value == null)
			throw new IllegalArgumentException("No property with key '" + property.toString() + "'");
		return value;
	}

	/**
	 * Store preferences; ensure perisistency
	 */
	public abstract void save();

	/**
	 * Overriding the default values with prefs from the last session.
	 */
	public abstract void load();
}
