package ch.cyberduck;

/*
 *  ch.cyberduck.Cyberduck.java
 *  Cyberduck
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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
 *
 *  @version    $Id$
 */

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

public class Cyberduck {

    public static final boolean VERBOSE = Boolean.getBoolean("ch.cyberduck.verbose");

    public static final String VERSIONID = "2.0b4";
    public static final String SPLASH = "cyberduck.splash.jpg";
    //public static final String HELP = "help.rtf";
    //public static final String HOMEPAGE = "index.html";
    public static final File PREFS_DIRECTORY = new File(System.getProperty("user.home"), ".cyberduck");
    //public static final String HISTORY_FILE = "cyberduck.history";
    public static final String PREFERENCES_FILE = "cyberduck.preferences";
    public static final String SEPARATOR = System.getProperty("line.separator");

    public static void main(String[] args) {
        try {
	    Cyberduck.DEBUG(System.getProperty("java.version"));
            // loading prefs
            if(Cyberduck.VERBOSE)
                Preferences.instance().list();
            // settting laf
            try {
                //javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
                //javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
                javax.swing.UIManager.setLookAndFeel(Preferences.instance().getProperty("laf.default"));
            }
            catch(Exception e) {
                System.out.println("Can't set the cross platform look and feel: " + e.getMessage());
            }
            // splash window
            DefaultBoundedRangeModel progressModel = new DefaultBoundedRangeModel(0, 0, 0, 100);
            SplashWindow splash = new SplashWindow(progressModel);
            splash.setVisible(true);
            splash.requestFocus();
            // loading app
            (new Initiator(splash, progressModel)).start();
        }
        catch(Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                                                      e.toString(),
                                                      "Initialization Exception",
                                                      javax.swing.JOptionPane.ERROR_MESSAGE
                                                      );
            System.exit(1);
        }
    }
    
    public static URL getResource(Class clazz, String name) {
        URL url= clazz.getResource(name);
        if (url == null) {
            Cyberduck.DEBUG("[Cyberduck] Warning: Failed to load resource '" + name + "'.");
        }
        return url;
    }

    public static URL getResource(String name) {
        return getResource(Cyberduck.class, name);
    }
    
    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    public static void DEBUG(String s) {
        if(VERBOSE) {
            System.out.println(s);
        }
    }
        
    public static Icon getIcon(URL url) {
        Icon image = null;
        try {
            image = new ImageIcon(url);
        }
        catch(Exception e) {
            Cyberduck.DEBUG("[Cyberduck] Warning: Failed to load image.");
        }
        return image;
    }
    
    public static String getVersion() {
        return VERSIONID;
    }
}
