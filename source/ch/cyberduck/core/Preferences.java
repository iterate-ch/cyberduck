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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Holding all application preferences. Default values get overwritten when loading
 * the <code>PREFERENCES_FILE</code>.
 * Singleton class.
 * @version $Id$
 */
public abstract class Preferences {//extends Properties {

    private static Logger log = Logger.getLogger(Preferences.class);

//    public static File PREFS_DIRECTORY = new File(System.getProperty("user.home"), ".cyberduck");
//    public static final String PREFERENCES_FILE = "cyberduck.preferences";

    //    private static File PREFS_DIRECTORY = null;
//    private static final String PREFERENCES_FILE = null;
    private static Preferences current = null;
//    private static Dimension screenSize = null;
//    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

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
        if(current == null) {
//            PREFS_DIRECTORY.mkdir();
            String strVendor = System.getProperty("java.vendor");
            if(strVendor.indexOf("Apple") != -1)
                current = new ch.cyberduck.ui.cocoa.CDPreferencesImpl();
            else
                current = new ch.cyberduck.ui.swing.PreferencesImpl();
            current.setDefaults();
            current.load();

            //initialize SSL
            /*
            String strVendor = System.getProperty("java.vendor");
            String strVersion = System.getProperty("java.version");
            //Assumes a system version string of the form:
            //[major].[minor].[release]  (eg. 1.2.2)
            Double dVersion = new Double(strVersion.substring(0, 3));
            //If we are running in a MS environment, use the MS stream handler.
            if(-1 < strVendor.indexOf("Microsoft")) {
                try {
                    Class clsFactory = Class.forName("com.ms.net.wininet.WininetStreamHandlerFactory");
                    if (null != clsFactory)
                        URL.setURLStreamHandlerFactory((URLStreamHandlerFactory)clsFactory.newInstance());
                }
                catch(Exception cfe) { //InstantiationException and ClassNotFoundException
                    log.warn("Unable to load the Microsoft SSL stream handler.");
                }
                //If the stream handler factory has
                //already been successfully set
                //make sure our flag is set and eat the error
            }
            //If we are in a normal Java environment,
            //try to use the JSSE handler.
            //NOTE:  JSSE requires 1.2 or better
            else if( 1.2 <= dVersion.doubleValue() ) {
                System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
                try {
                    //if we have the JSSE provider available, /and it has not already been set, add it as a new provide to the Security class.
                    Class clsFactory = Class.forName("com.sun.net.ssl.internal.ssl.Provider");
                    if( (null != clsFactory) && (null == Security.getProvider("SunJSSE")) )
                        Security.addProvider((Provider)clsFactory.newInstance());
                }
                catch(Exception cfe) {
                    log.warn("Unable to load the JSSE SSL stream handler.");
                }
            }
             */
        }
        return current;
    }

    public abstract void setProperty(String property, String value);

    public abstract void setProperty(String property, boolean v);
//        log.debug("setProperty(" + property + ", " + v + ")");
//        String value = "false";
//        if (v) {
//            value = "true";
//        }
//        this.put(property, value);
//    }

    public abstract void setProperty(String property, int v);
        //log.debug("setProperty(" + property + ", " + v + ")");
        //String value = String.valueOf(v);
        //this.put(property, value);
    //}

    /**
     * setting the default prefs values
     */
    public void setDefaults() {
        log.debug("setDefaults()");

	//	System.out.println("Working directory:"+System.getProperty("user.dir"));
	this.setProperty("cyberduck.version", "2.1");
        this.setProperty("cyberduck.uses", "0");
	this.setProperty("cyberduck.donate", "false");
        
	System.setProperty("sshtools.home", System.getProperty("user.dir"));
	System.setProperty("sshtools.config", System.getProperty("user.dir"));

        this.setProperty("laf.default", javax.swing.UIManager.getSystemLookAndFeelClassName());
        //this.setProperty("laf.default", javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
        this.setProperty("bookmarks.default", "My Bookmarks");
        this.setProperty("interface.multiplewindow", "false");
        this.setProperty("interface.error-dialog", "false");
        //Paths
        this.setProperty("download.path", System.getProperty("user.dir") + "/");

        // font sizes
        String font_small = "10";
        String font_normal = "12";
        if(System.getProperty("java.vendor").indexOf("Microsoft") != -1) {
            font_small = "12";
            font_normal = "12";
        }
        this.setProperty("font.small", font_small);
        this.setProperty("font.normal", font_normal);
        //Sound clips
        this.setProperty("status.sound.stop", "false");
        this.setProperty("status.sound.start", "true");
        this.setProperty("status.sound.complete", "true");
    
        //this.setProperty("files.encode", "true");
        this.setProperty("files.postprocess", "false");
        this.setProperty("files.removeCompleted", "false");
        
        //BookmarkTable properties
        //this.setProperty("table.save", "true");
        //BookmarkTable column locations
        /*
        this.setProperty("table.column0.position", "0");
        this.setProperty("table.column1.position", "1");
        this.setProperty("table.column2.position", "2");
        this.setProperty("table.column3.position", "3");
         */
        //BookmarkTable column widths
        this.setProperty("table.column0.width", "12");
        this.setProperty("table.column1.width", "500");
        this.setProperty("table.column2.width", "15");
        this.setProperty("table.column3.width", "150");
        
        //Duplicate files
        this.setProperty("duplicate.ask", "true");
        this.setProperty("duplicate.similar", "false");
        this.setProperty("duplicate.resume", "false");
        this.setProperty("duplicate.overwrite", "false");
        
        //Connection
        //this.setProperty("connection.log", "true");
        this.setProperty("connection.log.file", "cyberduck.connection.log");
        this.setProperty("connection.buffer", "4096");
//        this.setProperty("connection.log.speech", "false");
        this.setProperty("connection.port.default", "21");
        this.setProperty("connection.protocol.default", "ftp");
        this.setProperty("connection.transfertype.default", "binary");
//        this.setProperty("connection.timeout", "2"); // seconds
  //      this.setProperty("connection.timeout.default", "2"); // seconds
//        this.setProperty("connection.proxy", "false");
//        this.setProperty("connection.proxy.host", "proxy");
  //      this.setProperty("connection.proxy.port", "9999");
    //    this.setProperty("connection.proxy.authenticate", "false");
      //  this.setProperty("connection.proxy.username", "user");
      //  this.setProperty("connection.proxy.password", "pass");

        this.setProperty("connection.path.default", "~");

        this.setProperty("connection.login.name", System.getProperty("user.name"));
        this.setProperty("connection.login.anonymous.name", "anonymous");
        this.setProperty("connection.login.anonymous.pass", "user@domain.tld");
        
        //ftp properties
	this.setProperty("ftp.connectmode", "passive");
	//        this.setProperty("ftp.active", "true");
 //        this.setProperty("ftp.passive", "false");

	//listing properties
        this.setProperty("showHidden", "false");
        this.setProperty("listing.showType", "true");
        this.setProperty("listing.showFilenames", "true");
        this.setProperty("listing.showSize", "true");
        this.setProperty("listing.showDate", "true");
        this.setProperty("listing.showOwner", "true");
        this.setProperty("listing.showAccess", "false");
        
        //frame sizes
        this.setProperty("frame.width", "560");
        this.setProperty("frame.height", "480");
//        this.setProperty("frame.x", getXLocation(560));
//        this.setProperty("frame.y", getYLocation(480));
        this.setProperty("transcriptdialog.width", "520");
        this.setProperty("transcriptdialog.height", "550");
//        this.setProperty("transcriptdialog.x", getXLocation(520));
//        this.setProperty("transcriptdialog.y", getYLocation(550));
        this.setProperty("logdialog.width", "500");
        this.setProperty("logdialog.height", "300");
//        this.setProperty("logdialog.x", getXLocation(500));
//        this.setProperty("logdialog.y", getYLocation(300));
        this.setProperty("preferencesdialog.width", "500");
        this.setProperty("preferencesdialog.height", "485");
//        this.setProperty("preferencesdialog.x", getXLocation(500));
//        this.setProperty("preferencesdialog.y", getYLocation(485));
        this.setProperty("newconnectiondialog.width", "500");
        this.setProperty("newconnectiondialog.height", "300");
//        this.setProperty("newconnectiondialog.x", getXLocation(500));
//        this.setProperty("newconnectiondialog.y", getYLocation(300));
        
        //Status messages in detail panel
        //this.setProperty("statuspanel.localpath", "true");
        //this.setProperty("statuspanel.errormessage", "true");
        //this.setProperty("statuspanel.progressmessage", "true");
        //this.setProperty("statuspanel.transcriptmessage", "false");
    }
    

    public abstract String getProperty(String property);
        //log.debug("getProperty(" + property + ")");
        //String value = super.getProperty(property);
        //if(value == null)
        //    throw new IllegalArgumentException("No property with key '" + property.toString() + "'");
        //return value;
   // }
        
    /**
     * Save preferences into user home
     */
    public abstract void store();
        //log.debug("store()");
        //try {
        //    FileOutputStream output = new FileOutputStream(new File(PREFS_DIRECTORY, PREFERENCES_FILE));
        //    this.store(output, "Cyberduck properties - YOU SHOULD NOT EDIT THIS FILE");
        //    output.close();
        //}
        //catch(IOException e) {
        //    System.err.println("Could not save current preferences.\n" + e.getMessage());
        //}
    //}

    /**
     * Overriding the default values with prefs from the last session.
     */
    public abstract void load();
        //log.debug("load()");
        //try {
        //    File prefs = new File(PREFS_DIRECTORY, PREFERENCES_FILE);
        //    if (prefs.exists()) {
        //        this.load(new FileInputStream(prefs));
        //    }
        //    else {
        //        System.err.println("Could not load current preferences.");
        //    }
        //}
        //catch(IOException e) {
        //    System.err.println("Could not load current preferences.\n" + e.getMessage());
        //}
    //}

    public abstract void list();
    //    this.list(System.out);
    //}
}
