package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.ProgressPanel.java
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
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.connection.Status;
import ch.cyberduck.ui.common.DraggableLabel;
import ch.cyberduck.ui.common.GUIFactory;

/**
 * The ProgressPanel is the stanard detailed view of a bookmark displayed
 * in the bottom of the app's splitpane.
 * It contains a progressbar and labels with URL and local path
 * Must be registered to the BookmarkPanel as an Observer to be notified about
 * a bookmark's status
 * @version $Id$
 */
public class ProgressPanel extends JPanel implements Observer {
    private Bookmark selected;

    private JButton connectButton;
    private JLabel labelb;
    private JLabel labelc;
    private JLabel percentLabel;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private Action connectAction, stopAction;
    
    private JPanel paramPanel, buttonPanel;

    public ProgressPanel() {
        Cyberduck.DEBUG("[ProgressPanel]");
        this.init();
    }

    public void update(Observable o, Object argument) {
        if(o instanceof Bookmark) {
            if(argument.equals(Bookmark.SELECTION)) {
                this.selected = (Bookmark)o;
                this.refresh();
            }
        }
        if(o instanceof Status) {
            if (argument.equals(Status.CURRENT)) {
                this.progressBar.setModel(selected.status.getProgressModel());
                this.percentLabel.setText((int)(progressBar.getPercentComplete()*100) + "% ");
                this.progressLabel.setText(selected.status.getMessage(Status.DATA));
            }
            else if (argument.equals(Status.PROGRESS)) {
                this.progressLabel.setText(selected.status.getMessage(Status.DATA));
            }
            else { //@todo
                this.refresh();
            }
        }
        ((Observer)connectAction).update(o, argument);
        ((Observer)stopAction).update(o, argument);
    }

    public JButton getDefaultButton() {
        return this.connectButton;
    }
    
    /**
     * Update labels and progressbar with informations of the currently
     * selected bookmark
     */
    private void refresh() {
        progressBar.setModel(selected.status.getProgressModel());
        
        labelb.setText(selected.getAddressAsString());
        labelc.setText("Local: " + selected.getLocalPathAsString());
        percentLabel.setText((int)(progressBar.getPercentComplete()*100) + "% ");
        progressLabel.setText(selected.status.getMessage(Status.DATA));
        labelc.setVisible(selected.isDownload());
        progressLabel.setVisible(selected.isDownload());
    }

    private class ParameterPanel extends JPanel {
    	public ParameterPanel() {
    		super();
    		this.init();
        }
        private void init() {
            this.setLayout(new BorderLayout());
            JPanel barPanel = new JPanel();
            barPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            progressBar = new JProgressBar(0, 1);
            progressBar.setStringPainted(false);
            progressBar.setBorderPainted(true);
            barPanel.add(progressBar);
            barPanel.add(percentLabel = GUIFactory.labelBuilder("", GUIFactory.FONT_SMALL));

            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            Box labelBox = new Box(BoxLayout.Y_AXIS);
            //labelBox.add(labela = new DraggableLabel("", GUIFactory.FONT_SMALL));
            labelBox.add(labelb = new DraggableLabel("", GUIFactory.FONT_SMALL));
            labelBox.add(labelc = GUIFactory.labelBuilder("", GUIFactory.FONT_SMALL));
            labelBox.add(progressLabel = GUIFactory.labelBuilder("", GUIFactory.FONT_SMALL));
            labelPanel.add(labelBox);

            this.add(labelPanel, BorderLayout.CENTER);
            this.add(barPanel, BorderLayout.SOUTH);
        }
    }


    private class ButtonPanel extends JPanel {
        public ButtonPanel() {
            super();
            this.init();
        }
        private void init() {
            this.setLayout(new FlowLayout(FlowLayout.RIGHT));
            this.add(GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, stopAction = new StopAction()));
            this.add(connectButton = GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, connectAction = new ConnectAction()));
        }
        public Dimension getMinimumSize() {
            return new Dimension(super.getMinimumSize().width, 80);
        }
    }

    /**
        * Initialize the graphical user interface
     */
    private void init() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(paramPanel = new ParameterPanel());
        this.add(buttonPanel = new ButtonPanel());
    }

    /*
     public java.awt.Dimension getMinimumSize() {
         int h = (int)paramPanel.getMinimumSize().height + (int)buttonPanel.getMinimumSize().height;
    	int w = (int)super.getMinimumSize().width;
    	java.awt.Dimension d = new java.awt.Dimension(w, h);
//        java.awt.Dimension d = new java.awt.Dimension((int)super.getMinimumSize().width, 150);
        Cyberduck.DEBUG("[ProgressPanel] getMinimumSize():" + d.toString());
        return d;
    }
    */

    // ************************************************************************************************************

    private class ConnectAction extends AbstractAction implements java.util.Observer {
        public ConnectAction() {
            this("Connect");
        }

        public ConnectAction(String label) {
            super(label);
            this.putValue(SHORT_DESCRIPTION, "Connect to remote host");
            this.setEnabled(false);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }

        public void actionPerformed(ActionEvent ae) {
            selected.transfer();
        }

        public void update(java.util.Observable o, Object arg) {
            if (arg.equals(Status.ACTIVE) ||
                arg.equals(Status.STOP) ||
                arg.equals(Status.COMPLETE) ||
                arg.equals(Bookmark.SELECTION))
            {
                String name = "Connect";
                if(selected.isDownload()) {
                    if(selected.getHandler().equals(Status.RELOAD))
                        name = "Reload";
                    if(selected.getHandler().equals(Status.RESUME))
                        name = "Resume";
                    if(selected.getHandler().equals(Status.INITIAL))
                        name = "Download";
                }
                if(selected.isListing()) {
                    if(selected.getHandler().equals(Status.RELOAD))
                        name = "Refresh";
                    else
                        name = "Connect";
                }
                this.putValue(NAME, name);
                this.setEnabled(selected.isValid() && selected.status.isStopped());
            }
        }
    }


    private class StopAction extends AbstractAction implements java.util.Observer {
        public StopAction() {
            this("Stop");
        }

        public StopAction(String label) {
            super(label);
            this.putValue(SHORT_DESCRIPTION, "Stop transfer");
            //this.putValue(SMALL_ICON, Cyberduck.getIcon(Cyberduck.getResource("stop_small.gif")));
            this.setEnabled(false);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }

        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            selected.status.setCanceled(true);

            //((Action)ActionMap.instance().get("Stop")).actionPerformed(new ActionEvent(selected, ae.getID(), ae.getActionCommand()));
        }

        public void update(java.util.Observable o, Object arg) {
            if (arg.equals(Status.ACTIVE) ||
                arg.equals(Status.STOP) ||
                arg.equals(Status.COMPLETE) ||
                arg.equals(Bookmark.SELECTION))
            {
                //this.putValue(NAME, "Stop");
                this.setEnabled(!selected.status.isStopped());
            }
        }
    }

/*
    private class EditAction extends AbstractAction implements java.util.Observer {
        public EditAction() {
            this("Edit");
        }
        public EditAction(String label) {
            super(label);
            this.putValue(SHORT_DESCRIPTION, "Edit bookmark");
            this.setEnabled(false);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }

        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            selected.edit();
        }

        public void update(java.util.Observable o, Object arg) {
            if (arg.equals(Status.ACTIVE) ||
                arg.equals(Status.STOP) ||
                arg.equals(Status.COMPLETE) ||
                arg.equals(Bookmark.SELECTION))
            {
                this.setEnabled(selected.status.isStopped());
            }
        }
    }
    */

/*
    private class ShowInFinderAction extends javax.swing.AbstractAction implements java.util.Observer {
        public ShowInFinderAction() {
            super("Reveal In Finder");
            this.putValue(SHORT_DESCRIPTION, "Show file in Finder (Mac only)");
            this.setEnabled(System.getProperty("os.name").indexOf("Mac") != -1);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }

        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            try {
                com.apple.mrj.MRJFileUtils.openURL("file://" + selected.getLocalDirectory());
            }
            catch(java.io.IOException e) {
                e.printStackTrace();
            }
        }

        public void update(java.util.Observable o, Object arg) {
            if(arg.equals(Bookmark.SELECTION)) {
                boolean macos = System.getProperty("os.name").indexOf("Mac OS") != -1;
                this.setEnabled(((Bookmark)o).isDownload() && macos);
            }
        }
    }   
    */ 
}
