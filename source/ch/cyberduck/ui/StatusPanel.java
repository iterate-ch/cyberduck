package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.StatusPanel.java
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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import javax.swing.JFrame;
import javax.swing.JComponent;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.connection.Status;
import ch.cyberduck.ui.common.GUIFactory;

/**
  * The StatusPanel is the detailed view of a bookmark.
  * It contains in most cases a ProgressPanel. The appropriate
  * panel is determined according to the PANELPROPERTY in the bookmark's
  * status
  * @version $Id$
  */
public class StatusPanel extends JPanel implements Observer {
	private Container parent;
	            
    private JTabbedPane tabPane;
    private ListPanel listPanel;
    private LoginPanel loginPanel;
    private ProgressPanel progressPanel;
    private EditPanel editPanel;

    private ContentPanel contentPanel;
    private JPanel messagePanel;
    private JLabel statusLabel;
    
    private Bookmark selected;

    /**
     * The default constructor 
     */
    public StatusPanel() {
        Cyberduck.DEBUG("[StatusPanel()]");
        this.init();
        ObserverList.instance().registerObserver((Observer)this);
    }

    /**
        * New instance initialized with the properties of the @link{Bookmark} argument.
     * Instances created with this constructor will get notified by the Observable passed as
     * the argument.
     * @param bookmark The Observable and model of this panel
     * @see ch.cyberduck.connection.Bookmark 
     */
    public StatusPanel(Bookmark bookmark) {
        Cyberduck.DEBUG("[StatusPanel("+bookmark.toString()+")]");
        this.init();
        this.update(bookmark, Bookmark.SELECTION);
        bookmark.addObserver(this);
        bookmark.status.addObserver(this);
    }
    
    /**
    * @param parent The parent component
    */
    public void setParent(Container parent) {
    	this.parent = parent;
    }

    /**
	 * Initialize all subpanels
     */
    private void init() {
        contentPanel = new ContentPanel();
        editPanel = new EditPanel();
        messagePanel = new MessagePanel();
        this.setLayout(new BorderLayout());
        tabPane = new JTabbedPane() {
            public Dimension getMinimumSize() {
                Cyberduck.DEBUG("[JTabbedPane] getMinimumSize()");
                return this.getSelectedComponent().getMinimumSize();
            }
            public Dimension getPreferredSize() {
                Cyberduck.DEBUG("[JTabbedPane] getPreferredSize()");
                return this.getSelectedComponent().getPreferredSize();
            }
        };
        tabPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                Cyberduck.DEBUG(e.toString());
                resize();
            }
        });
//        tabPane.setBorder(BorderFactory.createEmptyBorder(0, -5, -5, -5));
        tabPane.setFont(GUIFactory.FONT_SMALL);
        tabPane.add("Progress", contentPanel);
        tabPane.add("Edit", editPanel);
        this.add(tabPane, BorderLayout.CENTER);
        this.add(messagePanel, BorderLayout.SOUTH);
        this.setVisible(false);
    }
    
    private class ContentPanel extends JPanel {
        public ContentPanel() {
            this.setLayout(new CardLayout());
            this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            this.add(progressPanel = new ProgressPanel(), Status.PROGRESSPANEL);
            this.add(listPanel = new ListPanel(), Status.LISTPANEL);
            this.add(loginPanel = new LoginPanel(), Status.LOGINPANEL);
        }

        /*
        protected void setDefaultButton() {
            if(this.getRootPane() != null) {
                String property = selected.status.getPanelProperty();
                if (property.equals(Status.PROGRESSPANEL)) {
                    this.getRootPane().setDefaultButton(progressPanel.getDefaultButton());
                    return;
                }
                if (property.equals(Status.LOGINPANEL)) {
                    this.getRootPane().setDefaultButton(loginPanel.getDefaultButton());
                    return;
                }
            }
        }
         */
        
        public Dimension getMinimumSize() {
            Cyberduck.DEBUG("[ContentPanel] getMinimumSize()");
            if(selected != null) {
                String property = selected.status.getPanelProperty();
                if (property.equals(Status.PROGRESSPANEL)) {
                    return progressPanel.getMinimumSize();
                }
                else if (property.equals(Status.LOGINPANEL)) {
                    return loginPanel.getMinimumSize();
                }
                else if (property.equals(Status.LISTPANEL)) {
                    return listPanel.getMinimumSize();
                }
            }
            return super.getMinimumSize();
        }
        
        public Dimension getPreferredSize() {
            Cyberduck.DEBUG("[ContentPanel] getPreferredSize()");
            if(selected != null) {
                String property = selected.status.getPanelProperty();
                if (property.equals(Status.PROGRESSPANEL)) {
                    return progressPanel.getPreferredSize();
                }
                else if (property.equals(Status.LOGINPANEL)) {
                    return loginPanel.getPreferredSize();
                }
                else if (property.equals(Status.LISTPANEL)) {
                    return listPanel.getPreferredSize();
                }
            }
            return super.getPreferredSize();
        }
    }

    private class MessagePanel extends JPanel {
        public MessagePanel() {
            this.setLayout(new GridLayout(1, 1));
            this.setBorder(BorderFactory.createCompoundBorder(
            	BorderFactory.createEmptyBorder(5, 5, 5, 5),
            	BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED))
            );
            this.add(statusLabel = GUIFactory.labelBuilder(ch.cyberduck.ui.common.GUIFactory.GRAY_ICON, "Idle", GUIFactory.FONT_SMALL));
        }
    }


    /**
        * Show the panel specified by the argument
     * @param property The panel property (specified as public static final in @link{Status})
     * @see ch.cyberduck.connection.Status
     */
    protected void showPanel(String property) {
        Cyberduck.DEBUG("[ContentPanel] showPanel("+property+")");
        CardLayout cl = (CardLayout)(contentPanel.getLayout());
        cl.show(contentPanel, property);
        tabPane.setSelectedIndex(tabPane.indexOfTab("Progress"));
        cl.invalidateLayout(contentPanel);
        resize();
        //requestFocus();
        //this.setDefaultButton();
    }
    
    /**
        * Resizes the parent component to fit this panel nicely.
     */
    protected void resize() {
        //        Cyberduck.DEBUG("Parent:\n"+this.getParent());
        if(parent == null)
            parent = this.getParent();
        if(parent != null) {
            if(parent instanceof JSplitPane) {
                ((JSplitPane)parent).resetToPreferredSizes();
            }
            if(parent instanceof JDialog) {
                ((JDialog)parent).pack();
            }
        }
    }
    
    /**
      * Get's called by either the download's or status's notifyObservers() method
      */
    public void update(Observable o, Object arg) {
//        Cyberduck.DEBUG("[StatusPanel] update("+arg.toString()+")");
        this.progressPanel.update(o, arg);
        this.listPanel.update(o, arg);
        this.editPanel.update(o, arg);
        this.loginPanel.update(o, arg);

        if(o instanceof Status) {
            if(o.equals(selected.status)) {
                if(arg.equals(Status.TIME) || arg.equals(Status.PROGRESS) || arg.equals(Status.ERROR)) {
                    statusLabel.setText(selected.status.getMessage(Status.TIME) + " : " + selected.status.getMessage(Status.PROGRESS) + " : " + selected.status.getMessage(Status.ERROR));
                }
                if(arg.equals(Status.COMPLETE) || arg.equals(Status.STOP) || arg.equals(Status.ACTIVE)) {
                    tabPane.setEnabledAt(tabPane.indexOfTab("Edit"), !arg.equals(Status.ACTIVE));
                    tabPane.setEnabledAt(tabPane.indexOfTab("Edit"), arg.equals(Status.STOP));
                    tabPane.setSelectedIndex(tabPane.indexOfTab("Progress"));
                    statusLabel.setIcon(selected.status.getIcon());
                }
                if (arg.equals(Status.PROGRESSPANEL) || arg.equals(Status.LISTPANEL) || arg.equals(Status.LOGINPANEL))
                    showPanel((String)arg);
            }
        }
        if(o instanceof Bookmark) {
            // display/switch to other transfer
            if(arg.equals(Bookmark.SELECTION)) {
                selected = (Bookmark)o;
                this.setVisible(true);
                showPanel(selected.status.getPanelProperty());
                statusLabel.setText(selected.status.getMessage(Status.TIME) + " : " + selected.status.getMessage(Status.PROGRESS) + " : " + selected.status.getMessage(Status.ERROR));
                tabPane.setEnabledAt(tabPane.indexOfTab("Edit"), selected.status.isStopped());
                statusLabel.setIcon(selected.status.getIcon());
            }
            if(arg.equals(Bookmark.DESELECTION)) {
                this.setVisible(false);
            }
        }
    }
}
