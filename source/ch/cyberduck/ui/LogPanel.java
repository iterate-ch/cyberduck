package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.LogPanel.java
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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Log;
import ch.cyberduck.ui.common.GUIFactory;

/**
* @version $Id$
*/
 public class LogPanel extends JPanel implements ActionListener {
    private JTextArea logField;

    public LogPanel() {
        Cyberduck.DEBUG("[LogPanel]");
        setLayout(new BorderLayout());

        JPanel log1 = new JPanel();
        log1.setLayout(new GridLayout(1, 1));

        JPanel log2 = new JPanel();
        log2.setLayout(new FlowLayout(FlowLayout.LEFT));

        logField = new JTextArea();
        logField.setEditable(false);
        logField.setFont(GUIFactory.FONT_MONOSPACED_SMALL);
        logField.setText(Log.open());
        logField.setSelectionStart(logField.getText().length());
        JScrollPane logScrollPane = new JScrollPane(logField);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        log2.add(GUIFactory.buttonBuilder("Load", GUIFactory.FONT_NORMAL, this));        
        log2.add(GUIFactory.buttonBuilder("Delete", GUIFactory.FONT_NORMAL, this));

        log1.add(logScrollPane);
        add(log1, BorderLayout.CENTER);
        add(log2, BorderLayout.SOUTH);
    }
    public void actionPerformed(ActionEvent e) {
        String source = e.getActionCommand();
	if(source.equals("Delete")) {
	    logField.setText("");
	    Log.delete();
	}
        if(source.equals("Load")) {
            logField.setText(Log.open());
            logField.setSelectionStart(logField.getText().length());
            logField.revalidate();
        }
    }
}
