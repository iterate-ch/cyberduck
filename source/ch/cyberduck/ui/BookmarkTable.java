package ch.cyberduck.ui;

/*
 *  ch.cyberduck.ui.BookmarkTable.java
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
import java.awt.dnd.*;
import java.awt.event.MouseEvent;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.ui.action.ActionMap;
import ch.cyberduck.ui.common.TableColumnSorter;
import ch.cyberduck.ui.model.BookmarkTableColumnModel;
import ch.cyberduck.ui.model.BookmarkTableModel;

/**
* @version $Id$
 */
public class BookmarkTable extends JTable {

    private BookmarkTable table = this;
    private BookmarkTableModel tableModel;
    private BookmarkTableColumnModel columnModel;
    
    public BookmarkTable() {
        super();
        Cyberduck.DEBUG("[BookmarkTable]");
        this.setModel(this.tableModel = new BookmarkTableModel());
        this.setColumnModel(this.columnModel = new BookmarkTableColumnModel());
        this.getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
  //      TableListener tl = new TableListener();
//        this.getSelectionModel().addListSelectionListener(tl);
	
        this.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            Bookmark last;
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    javax.swing.ListSelectionModel lsm = (javax.swing.ListSelectionModel)e.getSource();
                    if(!lsm.isSelectionEmpty()) {
                        int selectedRow = lsm.getMinSelectionIndex();
                        Bookmark bookmark = (Bookmark)tableModel.getEntry(selectedRow);
                        if(bookmark != null) {
                            if(last != null)
                                last.status.setSelected(false);
                            bookmark.status.setSelected(true);
                            bookmark.callObservers(Bookmark.SELECTION);
                            this.last = bookmark;
                        }
                    }
                    // no selection
                    else {
                        if(last != null) {
                            last.status.setSelected(false);
                            last.callObservers(Bookmark.DESELECTION);
                        }
                    }
                }
            }
        });

        final javax.swing.JPopupMenu popupMenu = new javax.swing.JPopupMenu();
        popupMenu.add(new javax.swing.JMenuItem((Action)ActionMap.instance().get("New Bookmark")));
        popupMenu.addSeparator();
        popupMenu.add(new javax.swing.JMenuItem((Action)ActionMap.instance().get("Connect Selected")));
//        popupMenu.add(new javax.swing.JMenuItem((Action)ActionMap.instance().get("Edit Selected")));
                
        DragSource.getDefaultDragSource().
            createDefaultDragGestureRecognizer(
                                this,
                                DnDConstants.ACTION_COPY_OR_MOVE,
                                new DragStartListener()
                                               );
        // make the columns sortable
        this.getTableHeader().addMouseListener(new TableColumnSorter(this, tableModel, columnModel));
    }

    public boolean editCellAt(int row, int col, java.util.EventObject event) {
        if(event instanceof MouseEvent) {
            //Cyberduck.DEBUG("***editCellAt --> MouseEvent");
            //Cyberduck.DEBUG("***editCellAt --> row:"+(row == this.getSelectedRow()));
            //Cyberduck.DEBUG("***editCellAt --> col:"+(col == this.getSelectedColumn()));
            if(row == this.getSelectedRow() && col == this.getSelectedColumn()) {
				if( ((MouseEvent)event).getClickCount() == 2) {
					//Cyberduck.DEBUG("---getClickCount()==2");
					Bookmark b = (Bookmark)tableModel.getEntry(row);
// cocoa-version					b.getStatusDialog().show();
					return false;
				}
				else if( ((MouseEvent)event).getClickCount() == 1) {
					//Cyberduck.DEBUG("---getClickCount()==1");
					return super.editCellAt(row, col, null);
	  //              this.editCellAt(row, col);
				}
			}
			else {
	            return super.editCellAt(row, col, event);
	        }
        }
        else if(event instanceof java.awt.event.KeyEvent) {
            //Cyberduck.DEBUG("***editCellAt --> KeyEvent");
        	return false;
        }
        return super.editCellAt(row, col, event);
//        Cyberduck.DEBUG("[BookmarkTable] editCellAt("+row+","+col+"):"+edit);
    }

    public BookmarkTableModel getTableModel() {
        return this.tableModel;
    }

    private class DragStartListener implements DragGestureListener {
        public void dragGestureRecognized(DragGestureEvent e) {
            Cyberduck.DEBUG(e.toString());
            if(getSelectedRowCount() == 1) {
                int row = getSelectedRow();
                int col = BookmarkTableColumnModel.ADDRESSCOLUMN;
                java.awt.Rectangle bounds = getCellRect(row, col, false);
                if(bounds.contains(e.getDragOrigin())) {
                    Object bookmark = tableModel.getEntry(row);
                    if (bookmark != null) {
                        String url = ((Bookmark)bookmark).getAddressAsString();
                        java.awt.Component rect = getCellRenderer(row, col).getTableCellRendererComponent(
                                                                                                          table,
                                                                                                          url,
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
                                    new java.awt.datatransfer.StringSelection(url), // transferable
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

    /**
     * Overwritten to return allways the size of the whole viewport. (Important for DND)
     */
    public boolean getScrollableTracksViewportHeight() {
        java.awt.Component parent = this.getParent();
        if (parent instanceof javax.swing.JViewport) {
            return parent.getHeight() > this.getPreferredSize().height;
        }
        return false;
    }
}
