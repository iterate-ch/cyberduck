package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.ListTable.java
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

import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.connection.Path;
import ch.cyberduck.connection.TransferAction;
import ch.cyberduck.ui.action.ActionMap;
import ch.cyberduck.ui.common.TableColumnSorter;
import ch.cyberduck.ui.model.ListTableColumnModel;
import ch.cyberduck.ui.model.ListTableModel;

/**
* @version $Id$
 */
public class ListTable extends JTable implements Observer {

	private ListTable table = this;
    private ListTableModel listModel = null;
    private ListTableColumnModel columnModel = null;

    private Bookmark selected = null;

    public void update(Observable o, Object arg) {
        if(o instanceof Bookmark) {
            if(arg.equals(Bookmark.SELECTION) || arg.equals(Bookmark.LIST)) {
                this.selected = (Bookmark)o;
                java.util.Iterator iterator = selected.getListing().iterator();
                int i = 0;
                Path p = null;
                listModel.clear();
                while(iterator.hasNext()) {
                    p = (Path)iterator.next();
                    if(p.isVisible()) {
                        listModel.addEntry(p, i);
                        i++;
                    }
                }
                listModel.fireTableDataChanged();
            }
        }
    }
    
    public ListTable() {
        Cyberduck.DEBUG("[ListTable] new ListTable()");
        this.setModel(listModel = new ListTableModel());
        this.setColumnModel(columnModel = new ListTableColumnModel());

        this.addKeyListener( 
            new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
//                    Cyberduck.DEBUG("[ListTable:KeyAdapter] keyReleased(" + e.toString() + ")");
                    e.consume();
                    int row = getSelectedRow();
                    if(e.isMetaDown() || e.isControlDown()) {
                        if(e.getKeyCode() == KeyEvent.VK_UP) {
                            handleListing(null, KeyEvent.VK_UP);
                        }
                        else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                            if(row > -1) {
                                Path p = (Path)listModel.getEntry(row);
                                if(p.isFile()) {
                                    handleDownload(p, row);
                                }
                                else if(p.isDirectory()) {
                                    handleListing(p, KeyEvent.VK_DOWN);
                                }
                            }
                        }
                    }
                }

                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    int selected[] = getSelectedRows();
                    if(e.getModifiers() != InputEvent.SHIFT_MASK) {
                        for(int i = (selected.length > 0 ? selected[selected.length-1] + 1 : 0); i < listModel.getRowCount(); i++) {
                            if(((String)listModel.getValueAt(i, ListTableColumnModel.FILENAMECOLUMN)).charAt(0) == c) {
                                setRowSelectionInterval(i, i);
                                return;
                            }
                        }
                        for(int i = 0; i < listModel.getRowCount(); i++) {
                            if(((String)listModel.getValueAt(i, ListTableColumnModel.FILENAMECOLUMN)).charAt(0) == c) {
                                setRowSelectionInterval(i, i);
                                return;
                            }
                        }
                    }
                    else {
                        for(int i = (selected.length > 0 ? selected[selected.length-1]-1 : 0); i >= 0; i--) {
                            if(((String)listModel.getValueAt(i, ListTableColumnModel.FILENAMECOLUMN)).charAt(0) == c) {
                                setRowSelectionInterval(i, i);
                                return;
                            }
                        }
                        for(int i = listModel.getRowCount() - 1; i >= 0; i--) {
                            if(((String)listModel.getValueAt(i, ListTableColumnModel.FILENAMECOLUMN)).charAt(0) == c) {
                                setRowSelectionInterval(i, i);
                                return;
                            }
                        }
                    }
                }
            }
        );
        JTableHeader tableHeader = this.getTableHeader();
        //make the columns sortable
        tableHeader.addMouseListener(new TableColumnSorter(this, listModel, columnModel));
        new DropTarget(this, DnDConstants.ACTION_MOVE, new FileDropTargetListener());
        DragSource.getDefaultDragSource().
            createDefaultDragGestureRecognizer(
                                this,
                                DnDConstants.ACTION_COPY_OR_MOVE,
                                new DragStartListener()
                                               );
    }

    public boolean editCellAt(int row, int col, java.util.EventObject event) {
        if(event instanceof MouseEvent) {
            if(row == this.getSelectedRow() && col == this.getSelectedColumn()) {
                if( ((MouseEvent)event).getClickCount() == 2) {
                    //Cyberduck.DEBUG("---getClickCount()==2");
                    Path p = (Path)listModel.getEntry(row);
                    if(p.isFile()) {
                        handleDownload(p, row);
                    }
                    else if(p.isDirectory()) {
                        handleListing(p, KeyEvent.VK_DOWN);
                    }
                    return false;
                }
                else if( ((MouseEvent)event).getClickCount() == 1) {
                    //Cyberduck.DEBUG("---getClickCount()==1");
                    return false;
                }
            }
            else {
                return super.editCellAt(row, col, event);
            }
        }
        else if(event instanceof java.awt.event.KeyEvent) {
        	return false;
        }
            return super.editCellAt(row, col, event);
//        Cyberduck.DEBUG("[BookmarkTable] editCellAt("+row+","+col+"):"+edit);
    }

    private void handleDownload(Path p, int row) {
        Bookmark t = selected.copy();
        t.setServerPath(p.toString());
        t.setLocalPath(new java.io.File(Preferences.instance().getProperty("download.path"), p.getName()));
        ((Action)(ActionMap.instance().get("New Bookmark"))).actionPerformed(new ActionEvent(t, ActionEvent.ACTION_PERFORMED, null));
        t.transfer(new TransferAction(TransferAction.GET));
    }
        
    private void handleListing(Path p, int key) {
        if(key == KeyEvent.VK_UP) {
            selected.transfer(new TransferAction(TransferAction.LIST, selected.getCurrentPath().getParent()));
        }
        else if(key == KeyEvent.VK_DOWN) {
            selected.transfer(new TransferAction(TransferAction.LIST, p));
        }
    }

    public boolean getScrollableTracksViewportHeight() {
        Component parent = this.getParent();
        if (parent instanceof javax.swing.JViewport) {
            return parent.getHeight() > this.getPreferredSize().getHeight();
        }
        return false;
    }


    private class FileDropTargetListener implements DropTargetListener {
        public void drop(DropTargetDropEvent e) {
            Cyberduck.DEBUG("[FileDropTargetListener] drop(" + e.toString() + ")");
            if(e.isLocalTransfer()) {
                Cyberduck.DEBUG("[FileDropTargetListener] isLocalTransfer(): true");
//                e.rejectDrop();
            }
            try {
                Transferable transferable = e.getTransferable();
                if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    Cyberduck.DEBUG("[FileDropTargetListener] DataFlavor.javaFileListFlavor");
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    int cursorRow = rowAtPoint(e.getLocation());
                    int selectedRow = getSelectedRow();
                    if((cursorRow == selectedRow) && (selectedRow != -1)) {
                        Path p = (Path)listModel.getEntry(selectedRow);
                        if(p.isDirectory()) {
                            selected.setCurrentPath(p);
                        }
                    }
                    java.util.List l = (java.util.List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    java.util.Iterator i = l.iterator();
                    while (i.hasNext()) {
                        selected.transfer(new TransferAction(TransferAction.PUT, i.next()), true);
                    }
                    selected.startQueue();
                    e.dropComplete(true);
                }
                if(transferable.isDataFlavorSupported(Path.pathFlavor)) {
                    Cyberduck.DEBUG("[FileDropTargetListener] Path.pathFlavor");
                    int cursorRow = rowAtPoint(e.getLocation());
                    int selectedRow = getSelectedRow();
                    if((cursorRow == selectedRow) && (selectedRow != -1)) {
                        Path dest = (Path)listModel.getEntry(selectedRow);
                        if(dest.isDirectory()) {
		                    Path orig = (Path)transferable.getTransferData(Path.pathFlavor);
				            if(!(dest.getPath().equals(orig.getPath()))) {
		        	            e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
								selected.transfer(new TransferAction(TransferAction.RNFR, orig, new Path(dest.getPath(), orig.getName())));
							}
		                }
                    }
                    e.dropComplete(true);
				}
                else {
                    Cyberduck.DEBUG("[FileDropTargetListener] Data flavor unknown");
                    e.rejectDrop();
                }
            }
            catch(java.io.IOException ioe) {
                ioe.printStackTrace();
                e.rejectDrop();
            }
            catch(UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
                e.rejectDrop();
            }
        }

        public void dragEnter(DropTargetDragEvent e) {
            Cyberduck.DEBUG(e.toString());
            if(e.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.red));
        }
        public void dragExit(DropTargetEvent e) {
            Cyberduck.DEBUG(e.toString());
            setBorder(null);
        }
        public void dragOver(DropTargetDragEvent e) {
            Cyberduck.DEBUG(e.toString());
            int row = rowAtPoint(e.getLocation());
            if(row != -1) {
                setRowSelectionInterval(row, row);
            }
        }
        public void dropActionChanged(DropTargetDragEvent e) {
            Cyberduck.DEBUG(e.toString());
            if(e.getDropAction() != DnDConstants.ACTION_COPY_OR_MOVE) {
                e.rejectDrag();
            }
        }
    }

    private class DragStartListener implements DragGestureListener {
        public void dragGestureRecognized(DragGestureEvent e) {
            Cyberduck.DEBUG(e.toString());
            if(getSelectedRowCount() == 1) {
                int row = getSelectedRow();
                int col = ListTableColumnModel.FILENAMECOLUMN;
                java.awt.Rectangle bounds = getCellRect(row, col, false);
                if(bounds.contains(e.getDragOrigin())) {
                    Path path = (Path)listModel.getEntry(row);
                    if (path != null) {
                        java.awt.Component rect = getCellRenderer(row, col).getTableCellRendererComponent(
                                                                                                          table,
                                                                                                          path.getName(),
                                                                                                          true,
                                                                                                          true,
                                                                                                          row,
                                                                                                          col
                                                                                                          );
                        rect.setSize(bounds.width, bounds.height);
                        java.awt.Image image = createImage(bounds.width, bounds.height);
                        java.awt.Graphics g = image.getGraphics();
                        rect.paint(g);
                        g.dispose();
                        e.startDrag(DragSource.DefaultMoveDrop, // cursor
                                    image,
                                    new java.awt.Point(0, 0), //offset
                                    path, // transferable
                                    new DragMoveListener()
                                    );  // drag source listener
                    }
                }
            }
        }
    }
    
    private class DragMoveListener implements DragSourceListener {
        public void dragDropEnd(DragSourceDropEvent e) {
            Cyberduck.DEBUG(e.toString());
            if(e.getDropSuccess()) {
            }
            else {
            }
        }
        public void dragExit(DragSourceEvent e) {
            Cyberduck.DEBUG(e.toString());
        }
        public void dragEnter(DragSourceDragEvent e) {
            Cyberduck.DEBUG(e.toString());
        }
        public void dragOver(DragSourceDragEvent e) {
            Cyberduck.DEBUG(e.toString());
        }
        public void dropActionChanged(DragSourceDragEvent e) {
            Cyberduck.DEBUG(e.toString());
        }
    }
}
