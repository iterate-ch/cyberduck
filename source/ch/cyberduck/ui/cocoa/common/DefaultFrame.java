package ch.cyberduck.ui.common;

/*
 *  ch.cyberduck.ui.common.DefaultFrame.java
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
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.event.*;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;

/**
 * Enhanced javax.swing.JDialog
 */
public class DefaultFrame extends javax.swing.JDialog {

    /** preferred winwow size <property>.width, <property>.height, <property>.x, <property>.y*/
    private String property;

    /**
     * @param title the title of the dialog
     * @param resizable enable window resizing
     */
    public DefaultFrame(String title, boolean resizable) {
        super();
        this.setTitle(title);
        this.setResizable(resizable);
        this.init();
    }

    /**
     * @param title the title of the dialog
     * @param resizable enable window resizing
     * @param property The name of the property to parse the
     * preferred winwow size <property>.width, <property>.height, <property>.x, <property>.y
     */
    public DefaultFrame(String title, boolean resizable, String property) {
        this(title, resizable);
        this.property = property;
        this.setSize(Integer.parseInt(Preferences.instance().getProperty(property + ".width")), Integer.parseInt(Preferences.instance().getProperty(property + ".height")));
        this.setLocation(Integer.parseInt(Preferences.instance().getProperty(property + ".x")), Integer.parseInt(Preferences.instance().getProperty(property + ".y")));
    }

    public Component add(Component content) {
        JPanel contentPanel = new JPanel();
//        contentPanel.setLayout(new java.awt.BorderLayout());
		contentPanel.setLayout(new GridLayout(1, 1));
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 15, 5));
        contentPanel.add(content);//, java.awt.BorderLayout.CENTER);
//        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(contentPanel);
        return content;
    }

    private void init() {
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(new WindowFix(),
                                                                 AWTEvent.WINDOW_EVENT_MASK);
        //this.setJMenuBar(CyberduckMenu.instance());
        this.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    exit();
                else if (e.isMetaDown() && (e.getKeyCode() == KeyEvent.VK_W))
                    exit();
                else if (e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_W)) {
                    exit();
                }
            }
        }
                       );
        this.addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                //System.out.println("*******SETTING JMenuBar");
                //setJMenuBar(CyberduckMenu.instance());
            }
            public void windowClosing(WindowEvent e) {
                exit();
            }
            public void windowDeactivated(WindowEvent e) {
                //System.out.println("*******REMOVING JMenuBar");
                //setJMenuBar(null);
            }
        }
                          );
    }

    public void exit() {
        Cyberduck.DEBUG("[DefaultFrame] exit()");
        if(this.property != null) {
            Preferences.instance().setProperty(property + ".width", getWidth());
            Preferences.instance().setProperty(property + ".height", getHeight());
            Preferences.instance().setProperty(property + ".x", getX());
            Preferences.instance().setProperty(property + ".y", getY());
        }
        this.dispose();
    }

    private class WindowFix implements AWTEventListener {
        private boolean fClosedOccured;
        public void eventDispatched(AWTEvent e) {
            WindowEvent we = (WindowEvent)e;
            if(we.WINDOW_ACTIVATED == we.getID()) {
                if(fClosedOccured) {
                    fClosedOccured = false;
                    we.getWindow().toFront();
                }
            }
            else if(we.WINDOW_CLOSING == we.getID()) {
                fClosedOccured = true;
            }
            else if(we.WINDOW_CLOSED == we.getID()) {
                fClosedOccured = true;
            }
        }
    }
}
