package ch.cyberduck.core;

/*
 *  ch.cyberduck.core.Trasncript.java
 *  Cyberduck
 *
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
 */

import javax.swing.JTextArea;

//import ch.cyberduck.ui.common.GUIFactory;

/**
 * Singleton text area to append logging messages.
 * @version $Id$
 */
public class Transcript extends JTextArea {

    private static Transcript instance;
    
    public static Transcript instance() {
        if(instance == null)
            instance = new Transcript();
        return instance;
     }

     private Transcript() {
         super();
         this.setEditable(true);
//         this.setFont(GUIFactory.FONT_MONOSPACED_SMALL);
     }

     public void transcript(String text) {
        //if(Preferences.instance().getProperty("connection.log").equals("true")) {
        this.append(text);
        this.setSelectionStart(this.getText().length());
    }
}
