package ch.cyberduck.ui.action;

/*
 *  ch.cyberduck.ShowPreferencesAction.java
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
import ch.cyberduck.ui.PreferencesPanel;
import ch.cyberduck.ui.common.DefaultFrame;
import ch.cyberduck.ui.common.GUIFactory;

/**
 * Show the preferences dialog
 * @see ch.cyberduck.ui.PreferencesPanel
 */
public class PreferencesAction extends AbstractAction {
    private javax.swing.JDialog dialog;
    
    public PreferencesAction() {
        super("Preferences");
        ActionMap.instance().put(this.getValue(NAME), this);
        putValue(SHORT_DESCRIPTION, "Edit application preferences");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, GUIFactory.MENU_MASK));
        //dialog.pack();
    }
    public void actionPerformed(ActionEvent ae) {
        Cyberduck.DEBUG(ae.paramString());
        if(dialog == null) {
            dialog = new DefaultFrame("Preferences", true, "preferencesdialog");
            dialog.add(new PreferencesPanel());
        }
        dialog.show();
    }
}
