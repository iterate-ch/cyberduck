package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.BookmarkPanel.java
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
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.connection.Status;
import ch.cyberduck.ui.action.ActionMap;
import ch.cyberduck.ui.common.GUIFactory;
import ch.cyberduck.ui.model.BookmarkTableColumnModel;
import ch.cyberduck.ui.model.BookmarkTableModel;
import ch.cyberduck.util.URLExporter;
import ch.cyberduck.util.URLImporter;

/**
 * The top component in the main windows's splitpane
 * Contiains a JTable for bookmark entries, a ComboBox for selecting
 * the bookmarks file to display and some buttons
 * @see ch.cyberduck.ui.BookmarkTable
 * @version $Id$
 */
public class BookmarkPanel extends JPanel implements Observer {

    private BookmarksSelectorModel bookmarksSelectorModel;
    private BookmarkTable bookmarkTable;
    
    //private List observers = new ArrayList();

    /**
        * The currently selected bookmark
     */
    private Bookmark selected;

    public BookmarkPanel() {
        Cyberduck.DEBUG("[BookmarkPanel]");
        this.init();
    }

    /**
        * Initialize all inner class actions
     */
    private void initActions() {
        new ExportAction();
        new ImportAction();
        new DeleteAllAction();
        new DeleteCompletedAction();
        new ConnectAllAction();
        new StopAllAction();
        
        new AddBookmarkFileAction();
        new RenameBookmarkFileAction();
        new DeleteBookmarkFileAction();
        new NewBookmarkAction();

        new OpenSelectedAction();
        new ConnectSelectedAction();
//        new EditSelectedAction();
        new StopSelectedAction();
        new DeleteSelectedAction();
    }

    public void update(Observable o, Object arg) {
        if(o instanceof Status) {
            if(arg.equals(Status.CURRENT)) {
                for (int i = 0; i <  ((BookmarkTableModel) ((BookmarkTableModel)bookmarkTable.getModel())).getRowCount(); i++) {
                     ((BookmarkTableModel)bookmarkTable.getModel()).fireTableCellUpdated(i, BookmarkTableColumnModel.PROGRESSCOLUMN);
                }
            }
            else if(arg.equals(Status.ACTIVE)) {
                for (int i = 0; i <  ((BookmarkTableModel)bookmarkTable.getModel()).getRowCount(); i++) {
                     ((BookmarkTableModel)bookmarkTable.getModel()).fireTableCellUpdated(i, BookmarkTableColumnModel.STATUSCOLUMN);
                }
            }
            else if(arg.equals(Status.STOP)) {
                for (int i = 0; i <  ((BookmarkTableModel)bookmarkTable.getModel()).getRowCount(); i++) {
                     ((BookmarkTableModel)bookmarkTable.getModel()).fireTableCellUpdated(i, BookmarkTableColumnModel.STATUSCOLUMN);
                }
            }
            else if(arg.equals(Status.COMPLETE)) {
                for (int i = 0; i <  ((BookmarkTableModel)bookmarkTable.getModel()).getRowCount(); i++) {
                     ((BookmarkTableModel)bookmarkTable.getModel()).fireTableCellUpdated(i, BookmarkTableColumnModel.STATUSCOLUMN);
                }
            }
        }
        if(o instanceof Bookmark) {
            // display/switch to other transfer
            if(arg.equals(Bookmark.SELECTION)) {
                selected = (Bookmark)o;
            }
        }
        ((Observer)ActionMap.instance().get("Connect Selected")).update(o, arg);
//        ((Observer)ActionMap.instance().get("Edit Selected")).update(o, arg);
        ((Observer)ActionMap.instance().get("Stop Selected")).update(o, arg);
    }

    /**
        * Accept drops of String flavors
     */
    private class DropListener implements DropTargetListener {
        public synchronized void drop(DropTargetDropEvent e) {
            Cyberduck.DEBUG(e.toString());
            if(e.isLocalTransfer()) {
                Cyberduck.DEBUG("[DropListener] isLocalTransfer=true)");
                e.rejectDrop();
            }
            try {
                Transferable transferable = e.getTransferable();
                if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    Cyberduck.DEBUG("[DropListener] DataFlavor.stringFlavor=true)");
                    // int row = table.rowAtPoint(e.getLocation());
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Bookmark transfer = new Bookmark();
                    transfer.setAddress((String)transferable.getTransferData(DataFlavor.stringFlavor));
                    ((Action)ActionMap.instance().get("New Bookmark")).actionPerformed(new ActionEvent(transfer, ActionEvent.ACTION_PERFORMED, null));
                    e.dropComplete(true);
                }
                else {
                    Cyberduck.DEBUG("[DropListener] DataFlavor.stringFlavor=false)");
                    e.rejectDrop();
                }
            }
            catch(java.io.IOException ioe) {
                ioe.printStackTrace();
                e.rejectDrop();
            }
            catch(java.awt.datatransfer.UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
                e.rejectDrop();
            }
        }

        public void dragEnter(DropTargetDragEvent e) {
            if(e.isDataFlavorSupported(DataFlavor.stringFlavor))
                bookmarkTable.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.red));
        }
        public void dragExit(DropTargetEvent e) {
            bookmarkTable.setBorder(null);
        }
        public void dragOver(DropTargetDragEvent e) {
            int row = bookmarkTable.rowAtPoint(e.getLocation());
            if(row != -1) {
                ((JTable)e.getDropTargetContext().getComponent()).setRowSelectionInterval(row, row);
            }
        }
        public void dropActionChanged(DropTargetDragEvent e) {
            Cyberduck.DEBUG(e.toString());
            if(e.getDropAction() != DnDConstants.ACTION_COPY_OR_MOVE) {
                e.rejectDrag();
            }
        }
    }
    
    
    /**
     * Initialize the graphical user interface
     */
    private void init() {
        this.initActions();

        JScrollPane tableScrollPane = new JScrollPane(this.bookmarkTable = new BookmarkTable());
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        new java.awt.dnd.DropTarget(bookmarkTable,
                                    java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE,
                                    new DropListener());
        
        //CONTROLPANEL
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(GUIFactory.buttonBuilder(GUIFactory.FONT_NORMAL, (Action)ActionMap.instance().get("New Bookmark")));
        controlPanel.add(GUIFactory.buttonBuilder("Connect", GUIFactory.FONT_NORMAL, (Action)ActionMap.instance().get("Connect Selected")));
//        controlPanel.add(GUIFactory.buttonBuilder("Edit", GUIFactory.FONT_NORMAL, (Action)ActionMap.instance().get("Edit Selected")));
        
        JComboBox bookmarksSelector = new JComboBox(this.bookmarksSelectorModel = new BookmarksSelectorModel());
        bookmarksSelector.setEditable(false);
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        this.add(bookmarksSelector, BorderLayout.NORTH);
        this.add(tableScrollPane, BorderLayout.CENTER);
        this.add(controlPanel, BorderLayout.SOUTH);
        ObserverList.instance().registerObserver((Observer)this);
    }

    private class BookmarksSelectorModel extends AbstractListModel implements ComboBoxModel {
        //hashmap containing different bookmark lists ('files') with associated key (string like 'My Bookmarks')
        Map bookmarkFiles = new HashMap();
        //key mapped to index
        List indexes = new ArrayList();
        //the currently selected boomark file key
        String file = null;

        public BookmarksSelectorModel() {
            super();
        }

        /**
         * @return key of the selected bookmark file
         */
        public Object getSelectedItem() {
            //Cyberduck.DEBUG("[BookmarksSelectorModel] getSelectedItem()");
            return file;
        }

        /**
         * @return key of the selected bookmark file at parameter index
         */
        public Object getElementAt(int index) {
            //Cyberduck.DEBUG("[BookmarksSelectorModel] getElementAt(" + index + ")");
            return indexes.get(index);
        }

        public void setSelectedItem(Object key) {
            Cyberduck.DEBUG("[BookmarksSelectorModel] setSelectedItem(" + key + ")");
            file = (String)key;
            if(selected != null)
                selected.callObservers(Bookmark.DESELECTION);
            ((BookmarkTableModel)bookmarkTable.getModel()).setData((List)this.get(key));
            Preferences.instance().setProperty("bookmarks.default", (String)key);
        }
        
        public int getSize() {
            //Cyberduck.DEBUG("[BookmarksSelectorModel] getSize()");
            return bookmarkFiles.size();
        }

        public Map getData() {
            Cyberduck.DEBUG("[BookmarksSelectorModel] getData()");
            return this.bookmarkFiles;
        }
        
        public void add(Object key, Object o) {
            Cyberduck.DEBUG("[BookmarksSelectorModel] add(" + key + ")");
            this.bookmarkFiles.put(key, o);
            this.indexes.add(indexes.size(), key);
            Iterator i = ((List)o).iterator();
            while(i.hasNext()) {
                //addObservers((Bookmark)i.next());
                 ObserverList map = ObserverList.instance();
                 Bookmark b = (Bookmark)i.next();
                 map.registerObservable(b);
                 map.registerObservable(b.status);
            }
            this.fireContentsChanged(this, 0, this.getSize());
        }

        public void remove(Object key) {
            Cyberduck.DEBUG("[BookmarksSelectorModel] removeElement(" + key + ")");
            this.indexes.remove(this.indexes.indexOf(key));
            this.bookmarkFiles.remove(key);
            this.fireContentsChanged(this, 0, this.getSize());
        }

        public boolean isEmpty() {
            return bookmarkFiles.isEmpty();
        }

        public Object get(Object key) {
            Cyberduck.DEBUG("[ComboPathModel] get(" + key + ")");
            return bookmarkFiles.get(key);
        }
    }

    /**
     * Store all bookmarks in <user.home>/Cyberduck.PREFS_DIRECTORY/<bookmark-name>.bookmarks
     */
    public void saveBookmarkFiles() {
        Cyberduck.DEBUG("[BookmarkPanel] saveBookmarkFiles()");
        FileOutputStream st1 = null;
        ObjectOutputStream st2 = null;
        Iterator keys = bookmarksSelectorModel.getData().keySet().iterator();
        Object key;
        List data;
        while(keys.hasNext()) {
            key = keys.next();
            Cyberduck.DEBUG("Saving bookmark file: " + key.toString());
            data = (List)bookmarksSelectorModel.get(key);
            try {
                st1 = new FileOutputStream(new File(Cyberduck.PREFS_DIRECTORY, (String)key + ".bookmarks"));
                st2 = new ObjectOutputStream(st1);
                Iterator iterator = data.iterator();
                while (iterator.hasNext()) {
                    Bookmark next = (Bookmark)iterator.next();
                    next.cleanup();
                    /*
                    if(! next.status.isStopped()) {
                        Object[] values = {"Quit", "Cancel"};
                        int option = JOptionPane.showOptionDialog(
                                                                  null,
                                                                  "Session to host " + next.getHost() + "\nnot closed.\nQuit anyway?",
                                                                  "Download in progress",
                                                                  JOptionPane.DEFAULT_OPTION,
                                                                  JOptionPane.QUESTION_MESSAGE,
                                                                  null,
                                                                  values,
                                                                  values[1]
                                                                  );
                        switch(option) {
                            case 1:
                                return false;
                        }
                    }
                     */
                    
                    /*
                    if(! next.status.isStopped()) {
                        Cyberduck.DEBUG("starting confirm thread");
                        ConfirmThread confirm = new ConfirmThread(next);
                        try {
                            javax.swing.SwingUtilities.invokeLater(confirm);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                        if(!confirm.quit) {
                            Cyberduck.DEBUG("returning false");
                            return false;
                        }
                        else
                            Cyberduck.DEBUG("continue");
                    }
                    Cyberduck.DEBUG("writing:"+next.toString());
                     */
                    st2.writeObject(next);
                }
            }
            catch(IOException e) {
                System.err.println("Problem saving boomarks: " + e.getMessage());
            }
            finally {
                try {
                    if (st1 != null)
                        st1.close();
                    if (st2 != null)
                        st2.close();
                }
                catch(IOException e) {
                    System.err.println("Problem closing output stream: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Load all bookmarks into the table.
     */
    public void loadBookmarkFiles() {
        Cyberduck.DEBUG("[BookmarkPanel] loadBoomarkFiles()");
        try {
            File f = new File(Cyberduck.PREFS_DIRECTORY, Preferences.instance().getProperty("bookmarks.default") + ".bookmarks");
            f.createNewFile();
        }
        catch(java.io.IOException e) {
            System.err.println("Error loading bookmarks: " + e.getMessage());
        }
        FileInputStream st1 = null;
        ObjectInputStream st2 = null;
        File path = Cyberduck.PREFS_DIRECTORY;
        if (path.exists()) {
            String[] files = path.list();
            for(int i = 0; i< files.length; i++) {
                int index;
                if((index = files[i].indexOf(".bookmarks")) != -1) {
                    File file = new File(path, files[i]);
                    Cyberduck.DEBUG("Loading " + file.toString());
                    List data = new ArrayList();
                    try {
                        st1 = new FileInputStream(file);
                        st2 = new ObjectInputStream(st1);
                        while(true) {
                            try {
                                Bookmark bookmark = (Bookmark)st2.readObject();
                                bookmark.status.fireStopEvent();
                                data.add(bookmark);
                            }
                            catch(ClassNotFoundException e) {
                                System.err.println("Error loading boomarks: " + e.getMessage());
                            }
                        }
                    }
                    catch(EOFException e) {}
                    catch(IOException e) {
                        Cyberduck.DEBUG("Error reading from '" + files[i] + "':  " + e.getMessage());
                    }
                    finally {
                        this.bookmarksSelectorModel.add(files[i].substring(0, index), data);
                        try {
                            if (st1 != null)
                                st1.close();
                            if (st2 != null)
                                st2.close();
                        }
                        catch(IOException e) {
                            System.err.println("Error closing output stream: " + e.getMessage());
                        }
                    }
                }
            }
            this.bookmarksSelectorModel.setSelectedItem(Preferences.instance().getProperty("bookmarks.default"));
        }
    }

    // ************************************************************************************************************


    private class OpenSelectedAction extends AbstractAction {
        public OpenSelectedAction() {
            super("Open Dialog");
            this.putValue(SHORT_DESCRIPTION, "Open new dialog for selected bookmarks.");
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, GUIFactory.MENU_MASK));
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            if(bookmarkTable.getSelectedRowCount() < 1)
                Cyberduck.beep();
            else {
                int[] selected = bookmarkTable.getSelectedRows();
                for (int i = 0; i < selected.length; i++) {
                    Bookmark b = (Bookmark)((BookmarkTableModel)bookmarkTable.getModel()).getEntry(selected[i]);
// cocoa-version                    b.getStatusDialog().show();
                }
            }
		}
    }

    private class NewBookmarkAction extends AbstractAction {
        public NewBookmarkAction() {
            super("New Bookmark");
            this.putValue(SHORT_DESCRIPTION, "Insert new bookmark");
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, GUIFactory.MENU_MASK));
            this.setEnabled(true);
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            Object source = ae.getSource();
            Bookmark bookmark;
            if(source instanceof Bookmark)
                bookmark = (Bookmark)source;
            else
                bookmark = new Bookmark();
            bookmarkTable.editingStopped(new javax.swing.event.ChangeEvent(this));
            int row = bookmarkTable.getSelectedRow();
            if(row != -1) {
                ((BookmarkTableModel)bookmarkTable.getModel()).addEntry(bookmark, row + 1);
                bookmarkTable.setRowSelectionInterval(row+1, row+1);
            }
            else {
                ((BookmarkTableModel)bookmarkTable.getModel()).addEntry(bookmark);
                bookmarkTable.setRowSelectionInterval(((BookmarkTableModel)bookmarkTable.getModel()).getRowCount() - 1,  ((BookmarkTableModel)bookmarkTable.getModel()).getRowCount() - 1);
            }
            ObserverList.instance().registerObservable(bookmark);
            ObserverList.instance().registerObservable(bookmark.status);
            bookmark.callObservers(Bookmark.SELECTION);
        }
    }

    private class AddBookmarkFileAction extends AbstractAction {
        public AddBookmarkFileAction() {
            super("New Bookmark File...");
            this.putValue(SHORT_DESCRIPTION, "Create new bookmarks file");
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            Object input = JOptionPane.showInputDialog(
                                                       null,
                                                       "Name of the new bookmark file:",
                                                       "New Bookmark file",
                                                       JOptionPane.QUESTION_MESSAGE,
                                                       null,
                                                       null,
                                                       null
                                                       );
            if(input != null) {
                File f = new File(Cyberduck.PREFS_DIRECTORY, (String)input + ".bookmarks");
                if(f.exists()) {
                    JOptionPane.showMessageDialog(
                                                  null,
                                                  "Please choose another name.",
                                                  "File already exists",
                                                  JOptionPane.ERROR_MESSAGE,
                                                  null
                                                  );
                }
                else {
                    bookmarksSelectorModel.add((String)input, new ArrayList());
                    bookmarksSelectorModel.setSelectedItem((String)input);
                }
            }
        }
    }

    private class DeleteBookmarkFileAction extends AbstractAction {
        public DeleteBookmarkFileAction() {
            super("Delete Bookmark File...");
            this.putValue(SHORT_DESCRIPTION, "Delete selected Bookmark file");
            this.setEnabled(true);
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            String selected = (String)bookmarksSelectorModel.getSelectedItem();
            int option = JOptionPane.showConfirmDialog(
                                                       null,
                                                       "Do you want to delete the\nbookmark file '" + selected + "'?",
                                                       "Delete bookmark file",
                                                       JOptionPane.WARNING_MESSAGE
                                                       );
            if(option == JOptionPane.OK_OPTION) {
                File f = new File(Cyberduck.PREFS_DIRECTORY, selected + ".bookmarks");
                f.delete();
                bookmarksSelectorModel.remove(selected);
                this.setEnabled(bookmarksSelectorModel.getSize() > 1);
                bookmarksSelectorModel.setSelectedItem(bookmarksSelectorModel.getElementAt(bookmarksSelectorModel.getSize()-1));
            }
        }
    }

    private class RenameBookmarkFileAction extends AbstractAction {
        public RenameBookmarkFileAction() {
            super("Rename Bookmark File...");
            this.putValue(SHORT_DESCRIPTION, "Rename selected Bookmark file");
            this.setEnabled(true);
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            String oldName = (String)bookmarksSelectorModel.getSelectedItem();
            Object newName = JOptionPane.showInputDialog(
                                                         null,
                                                         "Rename '" + oldName + "' to:",
                                                         "Rename Bookmark file",
                                                         JOptionPane.QUESTION_MESSAGE,
                                                         null,
                                                         null,
                                                         null
                                                         );
            if(newName != null) {
                File oldFile = new File(Cyberduck.PREFS_DIRECTORY, (String)oldName + ".bookmarks");
                File newFile = new File(Cyberduck.PREFS_DIRECTORY, (String)newName + ".bookmarks");
                if(newFile.exists()) {
                    JOptionPane.showMessageDialog(
                                                  null,
                                                  "Please choose another name.",
                                                  "File already exists",
                                                  JOptionPane.ERROR_MESSAGE,
                                                  null
                                                  );
                }
                else {
                    oldFile.renameTo(newFile);
                    bookmarksSelectorModel.add(newName, bookmarksSelectorModel.get(oldName));
                    bookmarksSelectorModel.remove(oldName);
                    bookmarksSelectorModel.setSelectedItem(newName);
                }
            }
        }
    }

    /**
        * Delete all selected bookmarks
     */
    private class DeleteSelectedAction extends AbstractAction {
        public DeleteSelectedAction() {
            super("Delete Selected");
            this.putValue(SHORT_DESCRIPTION, "Delete selected bookmarks.");
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, GUIFactory.MENU_MASK));
            //this.setEnabled(false);
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            if(bookmarkTable.getSelectedRowCount() < 1)
                Cyberduck.beep();
            else {
				bookmarkTable.editingStopped(new javax.swing.event.ChangeEvent(this));
				((BookmarkTableModel)bookmarkTable.getModel()).deleteSelected(bookmarkTable.getSelectedRows());
				if(bookmarkTable.getModel().getRowCount() > 0)
					bookmarkTable.setRowSelectionInterval(bookmarkTable.getModel().getRowCount() - 1, bookmarkTable.getModel().getRowCount() - 1);
        	}
        }
    }

    /**
        * Delete all bookmarks
     */
    private class DeleteAllAction extends AbstractAction {
        public DeleteAllAction() {
            super("Delete All");
            this.putValue(SHORT_DESCRIPTION, "Delete all bookmarks.");
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, GUIFactory.MENU_MASK | java.awt.Event.SHIFT_MASK));
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
			bookmarkTable.editingStopped(new javax.swing.event.ChangeEvent(this));
			((BookmarkTableModel)bookmarkTable.getModel()).clear();
			if(bookmarkTable.getModel().getRowCount() > 0)
				bookmarkTable.setRowSelectionInterval(bookmarkTable.getModel().getRowCount() - 1, bookmarkTable.getModel().getRowCount() - 1);
		}
    }

    /**
        * Delete all bookmarks which have the complete attribute set to true
     * @see ch.cyberduck.connection.Status#isComplete
     */
    private class DeleteCompletedAction extends AbstractAction {
        public DeleteCompletedAction() {
            super("Delete Completed");
            this.putValue(SHORT_DESCRIPTION, "Delete completed bookmarks.");
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
			bookmarkTable.editingStopped(new javax.swing.event.ChangeEvent(this));
			((BookmarkTableModel)bookmarkTable.getModel()).deleteCompleted();
			if(bookmarkTable.getModel().getRowCount() > 0)
				bookmarkTable.setRowSelectionInterval(bookmarkTable.getModel().getRowCount() - 1, bookmarkTable.getModel().getRowCount() - 1);
		}
    }

    /**
     * Read URLs from a text file and put them into currently selected table
     */
    private class ImportAction extends AbstractAction {
        public ImportAction() {
            super("Import...");
            this.putValue(SHORT_DESCRIPTION, "Extract URLs from text file.");
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, GUIFactory.MENU_MASK | java.awt.Event.SHIFT_MASK));
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            java.awt.FileDialog chooser = new java.awt.FileDialog(new java.awt.Frame(), "Import Bookmarks...", java.awt.FileDialog.LOAD);
            chooser.setTitle("Choose a text file to extract the URLs from:");
            chooser.setDirectory(System.getProperty("user.home"));
            chooser.setVisible(true);
            String resultPath = chooser.getDirectory();
            String resultFile = chooser.getFile();
            if(resultPath != null && resultFile != null) {
                File file = new File(resultPath, resultFile);
                if(file != null) {
                    URLImporter importer = new URLImporter(file, (Action)ActionMap.instance().get("New Bookmark"));
                    importer.start();
                }
            }
        }
    }

    /**
     * Export all bookmarks from the currently selected bookmark file into a text file
     */
    private class ExportAction extends AbstractAction {
        public ExportAction() {
            super("Export...");
            putValue(SHORT_DESCRIPTION, "Export bookmarks as text.");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, GUIFactory.MENU_MASK | java.awt.Event.SHIFT_MASK));
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            java.awt.FileDialog chooser = new java.awt.FileDialog(new java.awt.Frame(), "Export Bookmarks...", java.awt.FileDialog.SAVE);
            chooser.setDirectory(System.getProperty("user.home"));
            chooser.setFile("Cyberduck Bookmark List.txt");
            chooser.setVisible(true);
            String resultPath = chooser.getDirectory();
            String resultFile = chooser.getFile();
            if(resultPath != null && resultFile != null) {
                File file = new File(resultPath, resultFile);
                if(file != null) {
                    URLExporter exporter = new URLExporter(file,  ((BookmarkTableModel) ((BookmarkTableModel)bookmarkTable.getModel())));
                    exporter.start();
                }
            }
        }
    }

    // ************************************************************************************************************

    private class ConnectAllAction extends AbstractAction {
        public ConnectAllAction() {
            super("Connect All");
            this.putValue(SHORT_DESCRIPTION, "Connect to all bookmarks");
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            bookmarkTable.editingStopped(new javax.swing.event.ChangeEvent(this));
            int n = bookmarkTable.getRowCount();
            Bookmark t = null;
            for (int i = 0; i < n; i++) {
                t = (Bookmark)((BookmarkTableModel) bookmarkTable.getModel()).getEntry(i);
                t.transfer();
            }
        }
    }

    private class ConnectSelectedAction extends AbstractAction implements Observer {
        public ConnectSelectedAction() {
            super("Connect Selected");
            this.putValue(SHORT_DESCRIPTION, "Connect to all selected bookmarks");
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, GUIFactory.MENU_MASK));
            this.setEnabled(false);
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            if(bookmarkTable.getSelectedRowCount() < 1)
                Cyberduck.beep();
            else {
                int[] selected = bookmarkTable.getSelectedRows();
                for (int i = 0; i < selected.length; i++) {
                    Bookmark b = (Bookmark)((BookmarkTableModel)bookmarkTable.getModel()).getEntry(selected[i]);
                    b.transfer();
                }
            }
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

    private class StopAllAction extends AbstractAction {
        Bookmark selected = null;
        public StopAllAction() {
            super("Stop All");
            this.putValue(SHORT_DESCRIPTION, "Stop all active transfers");
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            int n = bookmarkTable.getRowCount() -1;
            for (int i = 0; i < n; i++) {
                Bookmark b = (Bookmark)((BookmarkTableModel)bookmarkTable.getModel()).getEntry(i);
                b.status.setCanceled(true);
//                ((Action)ActionMap.instance().get("Stop")).actionPerformed(new ActionEvent( ((BookmarkTableModel) ((BookmarkTableModel)bookmarkTable.getModel())).getEntry(i), ae.getID(), ae.getActionCommand()));
            }
        }
    }

    private class StopSelectedAction extends AbstractAction implements Observer {
        public StopSelectedAction() {
            super("Stop Selected");
            this.putValue(SHORT_DESCRIPTION, "Stop selected transfers");
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, GUIFactory.MENU_MASK));
            this.setEnabled(false);
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            if(bookmarkTable.getSelectedRowCount() < 1)
                Cyberduck.beep();
            else {
                int[] selected = bookmarkTable.getSelectedRows();
                for (int i = 0; i < selected.length; i++) {
                    Bookmark b = (Bookmark)((BookmarkTableModel)bookmarkTable.getModel()).getEntry(selected[i]);
                    b.status.setCanceled(true);

                    //((Action)ActionMap.instance().get("Stop")).actionPerformed(new ActionEvent( ((BookmarkTableModel) ((BookmarkTableModel)bookmarkTable.getModel())).getEntry(selected[i]), ae.getID(), ae.getActionCommand()));
                }
            }
        }
        public void update(java.util.Observable o, Object arg) {
            if (arg.equals(Status.ACTIVE) ||
                arg.equals(Status.STOP) ||
                arg.equals(Status.COMPLETE) ||
                arg.equals(Bookmark.SELECTION))
            {
                this.putValue(NAME, "Stop");
                this.setEnabled(!selected.status.isStopped());
            }
        }
    }
/*
    private class EditSelectedAction extends AbstractAction implements Observer {
        public EditSelectedAction() {
            super("Edit Selected");
            this.putValue(SHORT_DESCRIPTION, "Edit selected bookmark");
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, GUIFactory.MENU_MASK));
            this.setEnabled(false);
            ActionMap.instance().put(this.getValue(NAME), this);
        }
        public void actionPerformed(ActionEvent ae) {
            Cyberduck.DEBUG(ae.paramString());
            if(bookmarkTable.getSelectedRowCount() < 1)
                Cyberduck.beep();
            else {
                int[] selected = bookmarkTable.getSelectedRows();
                for (int i = 0; i < selected.length; i++) {
                    Bookmark b = (Bookmark)((BookmarkTableModel)bookmarkTable.getModel()).getEntry(selected[i]);
                    b.edit();
                    //((Action)ActionMap.instance().get("Edit")).actionPerformed(new ActionEvent( ((BookmarkTableModel) ((BookmarkTableModel)bookmarkTable.getModel())).getEntry(selected[i]), ae.getID(), ae.getActionCommand()));
                }
            }
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

    // ************************************************************************************************************

    public void saveProperties() {
        Preferences.instance().setProperty("table.column0.width", new Integer(this.bookmarkTable.getColumnModel().getColumn(BookmarkTableColumnModel.STATUSCOLUMN).getWidth()).toString());
        Preferences.instance().setProperty("table.column1.width", new Integer(this.bookmarkTable.getColumnModel().getColumn(BookmarkTableColumnModel.ADDRESSCOLUMN).getWidth()).toString());
        Preferences.instance().setProperty("table.column2.width", new Integer(this.bookmarkTable.getColumnModel().getColumn(BookmarkTableColumnModel.TYPECOLUMN).getWidth()).toString());
        Preferences.instance().setProperty("table.column3.width", new Integer(bookmarkTable.getColumnModel().getColumn(BookmarkTableColumnModel.PROGRESSCOLUMN).getWidth()).toString());
        /*
         Preferences.instance().setProperty("table.column0.position", new Integer(this.bookmarkTable.getColumnModel().getColumnIndexAtX(BookmarkTableColumnModel.STATUSCOLUMN)).toString());
         Preferences.instance().setProperty("table.column1.position", new Integer(this.bookmarkTable.getColumnModel().getColumnIndexAtX(BookmarkTableColumnModel.ADDRESSCOLUMN)).toString());
         Preferences.instance().setProperty("table.column2.position", new Integer(this.bookmarkTable.getColumnModel().getColumnIndexAtX(BookmarkTableColumnModel.TYPECOLUMN)).toString());
         Preferences.instance().setProperty("table.column3.position", new Integer(this.bookmarkTable.getColumnModel().getColumnIndexAtX(BookmarkTableColumnModel.PROGRESSCOLUMN)).toString());
         */
    }    
}
