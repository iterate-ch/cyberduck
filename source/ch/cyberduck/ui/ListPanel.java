package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.ListPanel.java
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.CyberduckMenu;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.connection.Path;
import ch.cyberduck.connection.TransferAction;
import ch.cyberduck.ui.action.ActionMap;
import ch.cyberduck.ui.common.GUIFactory;
import ch.cyberduck.ui.model.ListTableModel;

/**
* @version $Id$
 */
public class ListPanel extends JPanel implements Observer {
    
    private ListTable listingTable;
    private ListTableModel listingTableModel;

    /**
        * The currently selected bookmark
     */
    private Bookmark selected;

    private JScrollPane pane;
    private JComboBox comboBox;
    private ComboPathModel comboModel;
    private ActionListener comboActionListener;
    private ItemListener comboItemListener;

    private Action backAction;
    private Action upAction;
    private Action refreshAction;
    private Action newDirectoryAction;
    private Action renameAction;
    private Action deleteAction;
    private Action downloadAction;
    private Action permissionAction;
    private Action abortAction;
    private Action disconnectAction;
    
    public ListPanel() {
        super();
        this.init();
    }

    public void update(Observable o, Object arg) {
        this.listingTable.update(o, arg);
        if(arg.equals(Bookmark.SELECTION) || arg.equals(Bookmark.LIST)) {
            this.selected = (Bookmark)o;
            boolean valid = false;
            {
                valid = selected.isValid();
                valid = selected.getProtocol().equals(ch.cyberduck.connection.Session.FTP);
                valid = selected.isListing() && selected.getListing() != null;
            }
            backAction.setEnabled(valid);
            upAction.setEnabled(valid);
            refreshAction.setEnabled(valid);
            newDirectoryAction.setEnabled(valid);
            abortAction.setEnabled(valid);
            disconnectAction.setEnabled(valid);

            downloadAction.setEnabled(false);
            renameAction.setEnabled(false);
            deleteAction.setEnabled(false);
            permissionAction.setEnabled(false);            
            
            if(selected.isListing()) {
                Path cwd = selected.getCurrentPath();
                if(cwd != null) {
                    //@workaround because changing the model of JComboBox fires the ActionListener
                    this.comboBox.removeItemListener(comboItemListener);
                    this.comboModel.removeAll();
                    int depth = cwd.getPathDepth();
                    for(int i = 0; i <= depth; i++) {
                        this.comboModel.addElement(cwd.getPathFragment(i));
                    }
                    // selected the last added item
                    this.comboModel.setSelectedItem(comboModel.getElementAt(comboModel.getSize()-1));
                    this.comboBox.addItemListener(comboItemListener);
                }
            }
        }
    }

    public Dimension getPreferredSize() {
        Cyberduck.DEBUG("[ListPanel] getMinimumSize()");
        return listingTable.getPreferredScrollableViewportSize();
    }


    public Dimension getMinimumSize() {
        Dimension d = new Dimension(listingTable.getPreferredScrollableViewportSize().width, 300);
        Cyberduck.DEBUG("[ListPanel] getMinimumSize():" + d.toString());
        return d;
    }


    /* 
     * Initialize the graphical user interface; the table and associated TableModel
     */
    private void init() {
        Cyberduck.DEBUG("[ListPanel] init()");
        listingTable = new ListTable();
        listingTableModel = (ListTableModel)listingTable.getModel();
        this.setLayout(new BorderLayout());

        backAction = new BackAction();
        upAction = new UpAction();
        refreshAction = new RefreshAction();
        abortAction = new AbortAction();
        downloadAction = new ListingDownloadAction();
        newDirectoryAction = new NewDirectoryAction();
        renameAction = new RenameAction();
        deleteAction = new DeleteAction();
        permissionAction = new PermissionAction();
        disconnectAction = new DisconnectAction();

        this.updateFtpMenu();        

        ListSelectionModel selectionModel = listingTable.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    //Cyberduck.DEBUG(e.paramString());
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    boolean selection = !(lsm.isSelectionEmpty());

                    downloadAction.setEnabled(selection);
                    renameAction.setEnabled(selection);
                    deleteAction.setEnabled(selection);
                    permissionAction.setEnabled(selection);

                    updateFtpMenu();
                }
            }
        }
                                                );
        
        //table
        pane = new JScrollPane(listingTable);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        {
            final javax.swing.JPopupMenu popupMenu = new javax.swing.JPopupMenu();
            popupMenu.add(downloadAction);
            popupMenu.addSeparator();
            popupMenu.add(newDirectoryAction);
            popupMenu.add(renameAction);
            popupMenu.add(deleteAction);
            popupMenu.add(permissionAction);

            listingTable.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mousePressed(java.awt.event.MouseEvent e) {
                    maybeShowPopup(e);
                }
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    maybeShowPopup(e);
                }
                private void maybeShowPopup(java.awt.event.MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
                                          );
        }

        this.add(new NavigationPanel(), BorderLayout.NORTH);
        this.add(pane, BorderLayout.CENTER);
        this.add(new ButtonPanel(), BorderLayout.SOUTH);
    }

    private void updateFtpMenu() {
        final JMenuItem[] ftpMenuItems = {
            CyberduckMenu.instance().menuItemBuilder(newDirectoryAction),
            CyberduckMenu.instance().menuItemBuilder(renameAction),
            CyberduckMenu.instance().menuItemBuilder(deleteAction),
            CyberduckMenu.instance().menuItemBuilder(permissionAction),
            //null,
            CyberduckMenu.instance().menuItemBuilder(disconnectAction)
        };

        JMenu ftpMenu = CyberduckMenu.instance().getMenu(CyberduckMenu.FTP_MENU);
        ftpMenu.removeAll();
        for(int i = 0; i < ftpMenuItems.length; i++) {
            ftpMenu.add(ftpMenuItems[i]);
        }
    }

    private class ButtonPanel extends JPanel {
        public ButtonPanel() {
            super();
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            JButton downloadButton = GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, downloadAction);
            downloadButton.setIcon(new ImageIcon("ToolBarUI"));
            JButton newDirectoryButton = GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, newDirectoryAction);
            newDirectoryButton.setIcon(new ImageIcon("ToolBarUI"));
            JButton renameButton = GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, renameAction);
            renameButton.setIcon(new ImageIcon("ToolBarUI"));
            JButton deleteButton = GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, deleteAction);
            deleteButton.setIcon(new ImageIcon("ToolBarUI"));
            JButton permissionButton = GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, permissionAction);
            permissionButton.setIcon(new ImageIcon("ToolBarUI"));
            JButton disconnectButton = GUIFactory.buttonBuilder(GUIFactory.FONT_SMALL, disconnectAction);
            disconnectButton.setIcon(new ImageIcon("ToolBarUI"));

            this.add(downloadButton);
            this.add(newDirectoryButton);
            this.add(renameButton);
            this.add(deleteButton);
            this.add(permissionButton);
            this.add(disconnectButton);
        }
    }

    private class NavigationPanel extends JPanel {
        public NavigationPanel() {
            super();
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.add(GUIFactory.buttonBuilder(backAction));
            this.add(GUIFactory.buttonBuilder(upAction));
            this.add(GUIFactory.buttonBuilder(refreshAction));
            //directory selection
            comboBox = new JComboBox();
            comboBox.setEditable(false);
            comboBox.setModel(comboModel = new ComboPathModel());
            comboBox.setRenderer(new ComboPathRenderer());
            comboBox.addItemListener(comboItemListener = new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED) {
                        Cyberduck.DEBUG(e.paramString());
                        Path p = (Path)e.getItem();
                        selected.transfer(new TransferAction(TransferAction.LIST, p));
                    }
                }
            }
                                     );
            this.add(comboBox);
            this.add(GUIFactory.buttonBuilder(abortAction));
        }
    }

    // ************************************************************************************************************

    private class ComboPathRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if(value instanceof Path)  {
                Path p = (Path)value;
                if(p.getPathDepth() > 0) {
                    this.setIcon(new IndentIcon(GUIFactory.FOLDER_ICON, p.getPathDepth()));
                    this.setText(p.getName());
                }
                else {
                    this.setIcon(new IndentIcon(GUIFactory.HARDDRIVE_ICON, p.getPathDepth()));
                    this.setText(selected.getHost());
                }
            }
            return this;
        }
    };

    private class IndentIcon implements Icon {
        Icon icon = null;
        int depth = 0;
        
        public IndentIcon(Icon i, int d) {
            icon = i;
            depth = d;
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if(icon!=null)
                icon.paintIcon(c, g, 10 + x + depth * 10, y);
        }
        public int getIconWidth() {
            return 10 + depth * 10 + (icon != null ? icon.getIconWidth() : 0);
        }
        public int getIconHeight() {
            return (icon != null ? icon.getIconHeight() : 1);
        }
    }

    // ************************************************************************************************************

    private class ComboPathModel extends AbstractListModel implements MutableComboBoxModel {
        Object selected = null;
        Vector paths = new Vector();

        public int getSize() {
            return paths.size();
        }

        public void addElement(Object o) {
//            Cyberduck.DEBUG("[ComboPathModel] addElement(" + o + ")");
            paths.add(o);
//            this.fireIntervalAdded(this, this.getSize() - 1, this.getSize() - 1);
        }
        public void insertElementAt(Object o, int index) {
            Cyberduck.DEBUG("[ComboPathModel] insertElementAt(" + o + "," + index + ")");
            paths.add(index, o);
            this.fireIntervalAdded(this, index, index);
        }
        public void removeElement(Object o) {
            Cyberduck.DEBUG("[ComboPathModel] removeElement(" + o + ")");
            paths.removeElement(o);
            this.fireContentsChanged(this, 0, this.getSize());
        }
        public void removeElementAt(int index) {
            Cyberduck.DEBUG("[ComboPathModel] removeElementAt(" + index + ")");
            paths.removeElementAt(index);
            this.fireIntervalRemoved(this, index, index);
        }
        public void removeAll() {
            paths.clear();
            this.fireContentsChanged(this, 0, this.getSize());
        }
        public boolean isEmpty() {
            return paths.isEmpty();
        }
        public Object getElementAt(int index) {
//            Cyberduck.DEBUG("[ComboPathModel] getElementAt(" + index + ")");
            return paths.elementAt(index);//.getPath();
        }
        public void setSelectedItem(Object item) {
//            Cyberduck.DEBUG("[ComboPathModel] setSelectedItem(" + item + ")");
            this.selected = item;
            this.fireContentsChanged(this, 0, this.getSize());
//            comboBox.addActionListener(comboActionListener);
        }

        public Object getSelectedItem() {
//            Cyberduck.DEBUG("[ComboPathModel] getSelectedItem()");
            if(selected instanceof java.lang.String)
                return new Path((String)selected);
            return this.selected;
        }
    }


    // ************************************************************************************************************

    private class UpAction extends AbstractAction {
        public UpAction() {
//            super("List parent");
            this.putValue(SHORT_DESCRIPTION, "List parent directory");
            this.putValue(SMALL_ICON, Cyberduck.getIcon(Cyberduck.getResource(this.getClass(), "up_small.gif")));
            this.setEnabled(true);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            selected.transfer(new TransferAction(TransferAction.LIST, selected.getCurrentPath().getParent()));
        }
    }

    private class BackAction extends AbstractAction {
        public BackAction() {
//            super("Go Back");
            this.putValue(SHORT_DESCRIPTION, "List previous directory");
            this.putValue(SMALL_ICON, Cyberduck.getIcon(Cyberduck.getResource(this.getClass(), "back_small.gif")));
            this.setEnabled(true);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            selected.transfer(new TransferAction(TransferAction.LIST, selected.getPreviousPath()));
        }
    }

    private class RefreshAction extends AbstractAction {
        public RefreshAction() {
//            super("Refresh");
            this.putValue(SHORT_DESCRIPTION, "Refresh directory listing");
            this.putValue(SMALL_ICON, Cyberduck.getIcon(Cyberduck.getResource(this.getClass(), "refresh_small.gif")));
            this.setEnabled(true);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            selected.transfer(new TransferAction(TransferAction.LIST, comboModel.getElementAt(comboModel.getSize() - 1)));
        }
    }

    private class AbortAction extends AbstractAction {
        public AbortAction() {
            //super("Stop");
            this.putValue(SHORT_DESCRIPTION, "Abort last action");
            this.putValue(SMALL_ICON, Cyberduck.getIcon(Cyberduck.getResource(this.getClass(), "stop_small.gif")));
            this.setEnabled(true);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            //selected.transfer(new TransferAction(TransferAction.ABORT));
            selected.status.setCanceled(true);
            //((Action)ActionMap.instance().get("Stop")).actionPerformed(new ActionEvent(selected, ae.getID(), ae.getActionCommand()));
        }
    }
    
    private class NewDirectoryAction extends AbstractAction {
        public NewDirectoryAction() {
            super("New Folder");
            this.putValue(SHORT_DESCRIPTION, "Create new directory");
            //this.putValue(SMALL_ICON, GUIFactory.NEW_FOLDER_ICON);
            this.setEnabled(false);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            Object input = JOptionPane.showInputDialog(
                                                       null,
                                                       "Name of the new folder:",
                                                       "New Folder",
                                                       JOptionPane.QUESTION_MESSAGE,
                                                       null,
                                                       null,
                                                       null
                                                       );
            if(input != null) {
                if(input instanceof String) {
                    selected.transfer(new TransferAction(TransferAction.MKDIR, (String)input));
                }
            }
        }
    }
    
    private class DeleteAction extends AbstractAction {
        public DeleteAction() {
            super("Delete");
            this.putValue(SHORT_DESCRIPTION, "Delete file or folder");
            this.setEnabled(false);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            if(listingTable.getSelectedRowCount() < 1 )
                Cyberduck.beep();
            else {
                int[] rows = listingTable.getSelectedRows();
                StringBuffer candidates = new StringBuffer();
                for (int i = 0; i < rows.length; i++) {
                    candidates.append("\t"+listingTableModel.getEntry(rows[i])+"\n");
                    if(i > 10) {
                        candidates.append("\t... and "+(rows.length - i)+" more files.");
                        break;
                    }
                }
                String sep = System.getProperty("line.separator");
                int input = JOptionPane.showConfirmDialog(null,
                                                          "Do you really want to delete all selected files?"+sep+candidates.toString()+sep+"You cannot undo this action.",
                                                          "Delete",
                                                          JOptionPane.YES_NO_OPTION,
                                                          JOptionPane.QUESTION_MESSAGE
                                                          );
                if(input == JOptionPane.YES_OPTION) {
                    Path delete = null;
                    Cyberduck.DEBUG("[ListPanel] Deleting " + rows.length + " files");
                    for(int i = 0; i < rows.length; i++) {
                        delete = (Path)listingTableModel.getEntry(rows[i]);
                        selected.transfer(new TransferAction(TransferAction.DELE, delete), true);
                    }
                    selected.startQueue();
                }
            }
        }
    }

    private class RenameAction extends AbstractAction {
        public RenameAction() {
            super("Rename");
            this.putValue(SHORT_DESCRIPTION, "Rename file or folder");
            this.setEnabled(false);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            if(listingTable.getSelectedRowCount() > 1)
                Cyberduck.beep();
            if(listingTable.getSelectedRowCount() < 1)
                Cyberduck.beep();
            else {
                Path from = (Path)listingTableModel.getEntry(listingTable.getSelectedRow());
                Object input = JOptionPane.showInputDialog(null,
                                                           "Rename '" + from.getName() + "' to:",
                                                           "Rename",
                                                           JOptionPane.QUESTION_MESSAGE,
                                                           null,
                                                           null,
                                                           from.getName()
                                                           );
                if(input != null) {
                    if(input instanceof String) {
                        selected.transfer(new TransferAction(TransferAction.RNFR, from, new Path(from.getParent().getPath(), (String)input)));
                    }
                }
            }
        }
    }

    private class DisconnectAction extends AbstractAction {
        public DisconnectAction() {
            super("Disconnect");
            this.putValue(SHORT_DESCRIPTION, "Disconnect from remote host");
            this.setEnabled(false);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            selected.transfer(new TransferAction(TransferAction.QUIT));
        }
    }
    
    private class ListingDownloadAction extends AbstractAction {
        public ListingDownloadAction() {
            super("Download");
            this.putValue(SHORT_DESCRIPTION, "Download selected file or folder");
            this.setEnabled(false);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            if(listingTable.getSelectedRowCount() < 1 )
                Cyberduck.beep();
            else {
                Bookmark t = selected.copy();
                Path p = (Path)listingTableModel.getEntry(listingTable.getSelectedRow());
                t.setServerPath(p.toString());
                t.setLocalPath(new java.io.File(Preferences.instance().getProperty("download.path"), p.getName()));
                ((Action)(ActionMap.instance().get("New Bookmark"))).actionPerformed(new ActionEvent(t, ActionEvent.ACTION_PERFORMED, "New Bookmark"));
                t.transfer(new TransferAction(TransferAction.GET));
            }
        }
    }

    /**
     * Show permission dialog
     * @see ch.cyberduck.ui.PermissionDialog
     */
    private class PermissionAction extends AbstractAction {
        public PermissionAction() {
            super("Set Permissions");
            this.putValue(SHORT_DESCRIPTION, "Edit file permissions");
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, GUIFactory.MENU_MASK));
            this.setEnabled(false);
            //ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            if(listingTable.getSelectedRowCount() < 1 )
                Cyberduck.beep();
            else {
                Path file = (Path)listingTableModel.getEntry(listingTable.getSelectedRow());
                javax.swing.JDialog d = new PermissionDialog(selected, file);
                d.show();
            }
        }
    }
}
