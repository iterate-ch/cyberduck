package ch.cyberduck.ui.action;

/*
 *  ch.cyberduck.HelpAction.java
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
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.ui.common.GUIFactory;

public class HelpAction extends AbstractAction {
    public HelpAction() {
        super("Help");
        ActionMap.instance().put(this.getValue(NAME), this);
        this.putValue(SHORT_DESCRIPTION, "Show help");
        this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_HELP, GUIFactory.MENU_MASK));
    }
    
    public void actionPerformed(ActionEvent ae) {
        Cyberduck.DEBUG(ae.paramString());
        try {
            ch.cyberduck.util.BrowserLauncher.openURL("http://icu.unizh.ch/~dkocher/cyberduck/help/index.php");
        }
        catch(java.io.IOException ex) {
            ex.printStackTrace();
        }
    }
}
