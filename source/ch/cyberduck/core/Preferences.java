package ch.cyberduck.core;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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
public abstract class Preferences {//extends Properties {
    private static Logger log = Logger.getLogger(Preferences.class);
//    private static java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

    private static Preferences current = null;
    private Hashtable defaults;

    /**
        * Use #instance instead.
     */
    public Preferences() {
        super();
    }

    /**
     * @return The singleton instance of me.
     */
    public static Preferences instance() {
	log.debug("instance");
        if(null == current) {
            String strVendor = System.getProperty("java.vendor");
            if(strVendor.indexOf("Apple") != -1)
                current = new ch.cyberduck.ui.cocoa.CDPreferencesImpl();
            else
                current = new ch.cyberduck.ui.swing.PreferencesImpl();
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
        log.debug("setDefaults()");

	this.defaults = new Hashtable();

	//	System.out.println("Working directory:"+System.getProperty("user.dir"));
	defaults.put("version", "2.1");
        defaults.put("uses", "0");
	defaults.put("donate", "false");
	defaults.put("donate.url" , "http://www.cyberduck.ch/donate/");
        
	System.setProperty("sshtools.home", System.getProperty("user.dir"));
	System.setProperty("sshtools.config", System.getProperty("user.dir"));

        defaults.put("laf.default", javax.swing.UIManager.getSystemLookAndFeelClassName());
        //defaults.put("laf.default", javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
//        defaults.put("bookmarks.default", "My Bookmarks");
//        defaults.put("interface.multiplewindow", "false");
        defaults.put("interface.error-dialog", "false");
        //Paths
        defaults.put("download.path", System.getProperty("user.dir"));

        // font sizes
        String font_small = "10";
        String font_normal = "12";
        if(System.getProperty("java.vendor").indexOf("Microsoft") != -1) {
            font_small = "12";
            font_normal = "12";
        }
        defaults.put("font.small", font_small);
        defaults.put("font.normal", font_normal);
        //Sound clips
        defaults.put("status.sound.stop", "false");
        defaults.put("status.sound.start", "true");
        defaults.put("status.sound.complete", "true");
    
        //defaults.put("files.encode", "true");
        defaults.put("files.postprocess", "false");
        defaults.put("files.removeCompleted", "false");
        
        //BookmarkTable properties
        //defaults.put("table.save", "true");
        //BookmarkTable column locations
        /*
        defaults.put("table.column0.position", "0");
        defaults.put("table.column1.position", "1");
        defaults.put("table.column2.position", "2");
        defaults.put("table.column3.position", "3");
         */
        //BookmarkTable column widths
        defaults.put("table.column0.width", "12");
        defaults.put("table.column1.width", "500");
        defaults.put("table.column2.width", "15");
        defaults.put("table.column3.width", "150");
        
        //Duplicate files
        defaults.put("duplicate.ask", "true");
        defaults.put("duplicate.similar", "false");
        defaults.put("duplicate.resume", "false");
        defaults.put("duplicate.overwrite", "false");
        
        //Connection
        //defaults.put("connection.log", "true");
        defaults.put("connection.log.file", "cyberduck.connection.log");
        defaults.put("connection.buffer", "4096");
//        defaults.put("connection.log.speech", "false");
        defaults.put("connection.port.default", "21");
        defaults.put("connection.protocol.default", "ftp");
        defaults.put("connection.transfertype.default", "binary");
//        defaults.put("connection.timeout", "2"); // seconds
  //      defaults.put("connection.timeout.default", "2"); // seconds
//        defaults.put("connection.proxy", "false");
//        defaults.put("connection.proxy.host", "proxy");
  //      defaults.put("connection.proxy.port", "9999");
    //    defaults.put("connection.proxy.authenticate", "false");
      //  defaults.put("connection.proxy.username", "user");
      //  defaults.put("connection.proxy.password", "pass");

        defaults.put("connection.path.default", "~");

        defaults.put("connection.login.name", System.getProperty("user.name"));
        defaults.put("connection.login.anonymous.name", "anonymous");
        defaults.put("connection.login.anonymous.pass", "user@domain.tld");
        
        //ftp properties
//	defaults.put("ftp.connectmode", "passive");
	defaults.put("ftp.active", "true");
	defaults.put("ftp.passive", "false");

	//listing properties
        defaults.put("listing.showHidden", "false");
        defaults.put("listing.showType", "true");
        defaults.put("listing.showFilenames", "true");
        defaults.put("listing.showSize", "true");
        defaults.put("listing.showDate", "true");
	defaults.put("listing.showOwner", "true");
	defaults.put("listing.showAccess", "false");
        
        //frame sizes
        defaults.put("frame.width", "560");
        defaults.put("frame.height", "480");
//     @todo   defaults.put("frame.x", getXLocation(560));
//@todo        defaults.put("frame.y", getYLocation(480));
        defaults.put("transcriptdialog.width", "520");
        defaults.put("transcriptdialog.height", "550");
//        defaults.put("transcriptdialog.x", getXLocation(520));
//        defaults.put("transcriptdialog.y", getYLocation(550));
        defaults.put("logdialog.width", "500");
        defaults.put("logdialog.height", "300");
//        defaults.put("logdialog.x", getXLocation(500));
//        defaults.put("logdialog.y", getYLocation(300));
        defaults.put("preferencesdialog.width", "500");
        defaults.put("preferencesdialog.height", "485");
//        defaults.put("preferencesdialog.x", getXLocation(500));
//        defaults.put("preferencesdialog.y", getYLocation(485));
        defaults.put("newconnectiondialog.width", "500");
        defaults.put("newconnectiondialog.height", "300");
//        defaults.put("newconnectiondialog.x", getXLocation(500));
//        defaults.put("newconnectiondialog.y", getYLocation(300));
        
        //Status messages in detail panel
        //defaults.put("statuspanel.localpath", "true");
        //defaults.put("statuspanel.errormessage", "true");
        //defaults.put("statuspanel.progressmessage", "true");
        //defaults.put("statuspanel.transcriptmessage", "false");
    }
    

/**
	* Should be overriden by the implementation and only called if the property
 * can't be found in the users's defaults table
* @pram property The property to query.
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

    private String getXLocation(int componentWidth) {
	return null;
//        return new Integer((screenSize.width/2) - (componentWidth/2)).toString();
    }

    private String getYLocation(int componentHeight) {
	return null;
  //@todo      return new Integer((screenSize.height/2) - (componentHeight/2)).toString();
    }
    
}
