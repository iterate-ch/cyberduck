package ch.cyberduck.ui.dnd;

/*
 *  ch.cyberduck.ui.dnd.StringDragGestureListener.java
 *  Cyberduck
 *
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://dewww.epfl.ch/~dkocher/
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
 *  dkocher@mac.com
 */
 
import java.awt.dnd.*;
import java.awt.Rectangle;
import java.awt.Point;
import javax.swing.JTable;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.ui.dnd.*;
import ch.cyberduck.ui.model.BookmarkTableColumnModel;
import ch.cyberduck.ui.model.ExtendedTableModel;

public class StringDragGestureListener implements DragGestureListener {
    
    private ExtendedTableModel tableModel = null;
    private JTable table = null;

    public StringDragGestureListener(JTable table, ExtendedTableModel tableModel) {
        super();
        this.table = table;
        this.tableModel = tableModel;
    }
    
    public void dragGestureRecognized(DragGestureEvent e) {
        Cyberduck.DEBUG(e.toString());
        String selected = this.getSelectedAddress();
        java.awt.Point origin = e.getDragOrigin();
        if(selected != null) {
            Rectangle selectionRectangle = this.getSelectionRectangle();

//            Rectangle selectionRectangle = table.getCellRect(table.rowAtPoint(origin), table.columnAtPoint(origin), false);
            Cyberduck.DEBUG("*** Drag rectangle: " + selectionRectangle.toString());

            java.awt.Image dragImage = table.createImage((int)selectionRectangle.getWidth(), (int)selectionRectangle.getHeight());
            Cyberduck.DEBUG("*** Drag image: " + dragImage.toString());
            java.awt.Graphics g = dragImage.getGraphics();
            table.paint(g);
            e.startDrag(DragSource.DefaultMoveDrop, // cursor
                        dragImage,
                        new java.awt.Point(0, 0), //offset
                        new StringTransferable(this.getSelectedAddress()), // transferable
                        new URLDragSourceListener());  // drag source listener
            g.dispose();

            /*
            e.startDrag(DragSource.DefaultMoveDrop, // cursor
                        new StringTransferable(this.getSelectedAddress()), // transferable
                        new URLDragSourceListener());  // drag source listener
                        */
        }
        else {
            Cyberduck.beep();
        }
    }

    private Rectangle getSelectionRectangle() {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        return table.getCellRect(row, col, false);
    }

    private String getSelectedAddress() {
        if(table.getSelectedRowCount() != 1) {
            return null;
        }
        else {
            String address_string = (String)tableModel.getValueAt(table.getSelectedRow(), BookmarkTableColumnModel.ADDRESSCOLUMN);
            if(address_string != null && !address_string.equals("")) {
                return address_string;
            }
            else {
                return null;
            }
        }
    }
}
