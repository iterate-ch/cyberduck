package ch.cyberduck.ui.action;

/*
 *  ch.cyberduck.ShowTranscriptAction.java
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
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Transcript;
import ch.cyberduck.ui.common.DefaultFrame;
import ch.cyberduck.ui.common.GUIFactory;

/**
 * Show transcript dialog
 * @see ch.cyberduck.connection.Transcript
 */
public class ShowTranscriptAction extends AbstractAction {
    private javax.swing.JDialog dialog;
    public ShowTranscriptAction() {
        super("Show Transcript");
        ActionMap.instance().put(this.getValue(NAME), this);
        putValue(SHORT_DESCRIPTION, "Show transfer transcript.");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, GUIFactory.MENU_MASK));
    }
    public void actionPerformed(ActionEvent ae) {
        Cyberduck.DEBUG(ae.paramString());
        if(dialog == null) {
            //JPanel panel = new JPanel();
            //panel.setLayout(new BorderLayout());
            JScrollPane scrollpane = new JScrollPane(Transcript.instance());
            scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            //panel.add(scrollpane, BorderLayout.CENTER);
            dialog = new DefaultFrame("Transcript", true, "transcriptdialog");
            dialog.add(scrollpane);
        }
        dialog.show();
    }
}
