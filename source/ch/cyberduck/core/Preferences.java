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

import java.util.Hashtable;

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

    /**
	* @return The singleton instance of me.
     */
    public static Preferences instance() {
	if(null == current) {
//	    String strVendor = System.getProperty("java.vendor");
  //          if(strVendor.indexOf("Apple") != -1)
	    current = new ch.cyberduck.ui.cocoa.CDPreferencesImpl();
//	    else
//		current = new ch.cyberduck.ui.swing.PreferencesImpl();
	    current.setDefaults();
            current.load();
//	System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", Preferences.instance().getProperty("http.wire.logging"));
//	System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", Preferences.instance().getProperty("http.logging"));
//	System.setProperty("rendezvous.debug", Preferences.instance().getProperty("rendezvous.logging"));
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
//        log.debug("setDefaults()");

	this.defaults = new Hashtable();

//	defaults.put("logging", "DEBUG");
//	defaults.put("logging", "INFO");
	defaults.put("logging", "WARN");
//	defaults.put("logging", "ERROR");
//	defaults.put("logging", "FATAL");
	defaults.put("http.logging", "WARN");
	defaults.put("http.wire.logging", "WARN");
	defaults.put("rendezvous.logging", "0");

	defaults.put("version", "2.1");
        defaults.put("uses", "0");
	defaults.put("donate", "true");
	defaults.put("mail", "mailto:dkocher@cyberduck.ch");
//	defaults.put("website.donate" , "http://www.cyberduck.ch/donate/");
	defaults.put("website.donate" , "https://www.paypal.com/xclick/business=dkocher%40cyberduck.ch&item_name=Cyberduck&item_number=Cyberduck");
	defaults.put("website.update.xml" , "http://update.cyberduck.ch/versionlist.xml");
	defaults.put("website.update" , "http://cyberduck.ch/");
	defaults.put("website.home" , "http://cyberduck.ch/");

	defaults.put("browser.opendefault", "true");
	defaults.put("browser.showHidden", "false");
	defaults.put("transfer.close", "false");
	
	defaults.put("history.size", "10");
	defaults.put("history.save", "true");

	defaults.put("favorites.save", "true");

        defaults.put("connection.login.name", System.getProperty("user.name"));
        defaults.put("connection.download.folder", System.getProperty("user.home"));
//        defaults.put("download.duplicate.ask", "true");
//	defaults.put("download.duplicate.similar", "false");
//	defaults.put("download.duplicate.resume", "false");
//	defaults.put("download.duplicate.overwrite", "false");
	defaults.put("download.duplicate", "similar");
	
        //defaults.put("files.encode", "true");
	defaults.put("connection.download.postprocess", "false");
        //Connection
        defaults.put("connection.buffer", "1024");
	defaults.put("connection.buffer.default", "1024");
	defaults.put("connection.port.default", "21");
        defaults.put("connection.protocol.default", "ftp");
//        defaults.put("connection.path.default", "~");
        
        //ftp properties
        defaults.put("ftp.anonymous.name", "anonymous");
        defaults.put("ftp.anonymous.pass", "user@domain.net");
	defaults.put("ftp.connectmode", "passive");
        defaults.put("ftp.transfermode", "binary");

	defaults.put("ssh.knownhosts", System.getProperty("user.home")+"/.ssh/known_hosts");

//@todo	defaults.put("ssh.encryption", "aes128-cbc");
//@todo	defaults.put("ssh.encryption.authentication", "hmac-md5");
//@todo	defaults.put("ssh.compression", "zlib");
//@todo	defaults.put("ssh.authentication", "password");
	
//	Supported Encryption Client->Server [aes128-cbc, 3des-cbc, blowfish-cbc, cast128-cbc, arcfour, aes192-cbc, aes256-cbc, rijndael-cbc@lysator.liu.se]
//	    Supported Encryption Server->Client [aes128-cbc, 3des-cbc, blowfish-cbc, cast128-cbc, arcfour, aes192-cbc, aes256-cbc, rijndael-cbc@lysator.liu.se]
//	    Supported Mac Client->Server [hmac-md5, hmac-sha1, hmac-ripemd160, hmac-ripemd160@openssh.com, hmac-sha1-96, hmac-md5-96]
//	    Supported Mac Server->Client [hmac-md5, hmac-sha1, hmac-ripemd160, hmac-ripemd160@openssh.com, hmac-sha1-96, hmac-md5-96]
	    // [publickey, password, keyboard-interactive]

    }
    

/**
	* Should be overriden by the implementation and only called if the property
 * can't be found in the users's defaults table
* @param property The property to query.
*	@return The value of the property
*/
    public String getProperty(String property) {
	String value = (String)defaults.get(property);
        if(value == null)
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

//    private String getXLocation(int componentWidth) {
//        return new Integer((screenSize.width/2) - (componentWidth/2)).toString();
 //   }

//    private String getYLocation(int componentHeight) {
  //      return new Integer((screenSize.height/2) - (componentHeight/2)).toString();
  //  }
}
