package ch.cyberduck.ui.action;

/*
 *  ch.cyberduck.AboutAction.java
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

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.SplashWindow;

/**
 * @see ch.cyberduck.SplashWindow
 */
public class AboutAction extends AbstractAction {
    private javax.swing.JWindow splash; 
    
    public AboutAction() {
        super("About");
        ActionMap.instance().put(this.getValue(NAME), this);
        putValue(SHORT_DESCRIPTION, "About Cyberduck");
    }
    
    public void actionPerformed(ActionEvent ae) {
        Cyberduck.DEBUG(ae.paramString());
        if(splash == null)
            splash = new SplashWindow();
        splash.show();
    }
}

