package ch.cyberduck.ui.swing;

/*
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
 */

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import org.apache.log4j.Logger;

import ch.cyberduck.core.Preferences;

/**
* @version $Id$
 */
public class Cyberduck {
    private static Logger log = Logger.getLogger(Cyberduck.class);

    static {
	org.apache.log4j.BasicConfigurator.configure();
    }
    
    public static final String SPLASH = "cyberduck.splash.jpg";

    public static void main(String[] args) {
        try {
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
        
    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

//    public static void DEBUG(String s) {
//        if(VERBOSE) {
//            System.out.println(s);
//        }
//    }
        //    public static String getVersion() {
//        return VERSIONID;
//    }
}
