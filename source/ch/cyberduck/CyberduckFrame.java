package ch.cyberduck;

/*
 *  ch.cyberduck.CyberduckFrame.java
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

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import ch.cyberduck.connection.Status;
import ch.cyberduck.ui.BookmarkPanel;
import ch.cyberduck.ui.ObserverList;
import ch.cyberduck.ui.StatusPanel;
import ch.cyberduck.ui.common.GUIFactory;
import ch.cyberduck.ui.action.*;
import ch.cyberduck.ui.action.ActionMap;

public class CyberduckFrame extends JFrame {

    private BookmarkPanel bookmarkPanel;
    private StatusPanel statusPanel;
    private JSplitPane mainPane;

    public CyberduckFrame() {
        this.init();
    }

    private void init() {
        this.setTitle("Cyberduck "+Cyberduck.getVersion());
        this.setResizable(true);
        this.setSize(Integer.parseInt(Preferences.instance().getProperty("frame.width")), Integer.parseInt(Preferences.instance().getProperty("frame.height")));
        this.setLocation(Integer.parseInt(Preferences.instance().getProperty("frame.x")), Integer.parseInt(Preferences.instance().getProperty("frame.y")));
    }

    public void initListeners() {
        Cyberduck.DEBUG("[CyberduckFrame] initListeners()");
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ((Action)ActionMap.instance().get("Quit")).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Quit"));
            }

        }
                               );
        if(com.apple.mrj.MRJApplicationUtils.isMRJToolkitAvailable()) {
            com.apple.mrj.MRJApplicationUtils.registerQuitHandler(new com.apple.mrj.MRJQuitHandler() {
                public void handleQuit() {
                    ((Action)ActionMap.instance().get("Quit")).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Quit"));
                }
            }
                                                                  );
            com.apple.mrj.MRJApplicationUtils.registerAboutHandler(new com.apple.mrj.MRJAboutHandler() {
                public void handleAbout() {
                    ((Action)ActionMap.instance().get("About")).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "About"));
                }
            }
                                                                   );
            com.apple.mrj.MRJApplicationUtils.registerPrefsHandler(new com.apple.mrj.MRJPrefsHandler() {
                public void handlePrefs() {
                    ((Action)ActionMap.instance().get("Preferences")).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Preferences"));
                }
            }
                                                                   );
        }
    }

    /**
        * Initialize all inner class actions
     */
    public void initActions() {
        Cyberduck.DEBUG("[CyberduckFrame] initActions()");
//        new NewConnectionAction();
        new InterfaceAction();
        new PreferencesAction();
        new QuitAction();
        new AboutAction();
        new HelpAction();
        new WebsiteAction();
        new ShowTranscriptAction();
        new ShowLogAction();
    }

    public void initBookmarks() {
        Cyberduck.DEBUG("[CyberduckFrame] initBookmarks()");
        this.bookmarkPanel.loadBookmarkFiles();
    }

    public void initMainPanels() {
        Cyberduck.DEBUG("[CyberduckFrame] initMainPanels()");
        if(bookmarkPanel == null)
            bookmarkPanel = new BookmarkPanel();
        if(Preferences.instance().getProperty("interface.multiplewindow").equals("true")) {
            bookmarkPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
            this.getContentPane().setLayout(new BorderLayout());
            this.getContentPane().add(bookmarkPanel, BorderLayout.CENTER);
        }
        if(Preferences.instance().getProperty("interface.multiplewindow").equals("false")) {
            if(statusPanel == null)
                statusPanel = new StatusPanel();
            statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            mainPane = new JSplitPane(
                                      JSplitPane.VERTICAL_SPLIT,
                                      true, //continuous layout
                                      bookmarkPanel,
                                      statusPanel);
            //mainPane.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
            mainPane.setOneTouchExpandable(true);
            mainPane.setResizeWeight(1); // give extra space to the addressTable (top component)
            this.getContentPane().setLayout(new BorderLayout());
            this.getContentPane().add(mainPane, BorderLayout.CENTER);
        }
        this.setJMenuBar(CyberduckMenu.instance());
    }

    // ************************************************************************************************************

    private class InterfaceAction extends AbstractAction {
        public InterfaceAction() {
            super("Reconfigure Interface");
            this.putValue(SHORT_DESCRIPTION, "Change the window mode");
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            getContentPane().removeAll();
            initMainPanels();
            validate();
            repaint();
        }
    }
    
    private class QuitAction extends AbstractAction {
        public QuitAction() {
            super("Quit");
            this.putValue(SHORT_DESCRIPTION, "Exit Cyberduck");
            ActionMap.instance().put(this.getValue(NAME), this);
        }

        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            Preferences.instance().setProperty("frame.x", new Integer(getX()).toString());
            Preferences.instance().setProperty("frame.y", new Integer(getY()).toString());
            Preferences.instance().setProperty("frame.width", new Integer(getWidth()).toString());
            Preferences.instance().setProperty("frame.height", new Integer(getHeight()).toString());
            Preferences.instance().store();
            bookmarkPanel.saveBookmarkFiles();
            bookmarkPanel.saveProperties();
            dispose();
            System.exit(0);
        }
    }
}
