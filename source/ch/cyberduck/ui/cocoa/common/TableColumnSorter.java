package ch.cyberduck.ui.common;

/*
 *  ch.cyberduck.ui.table.TableColumnSorter.java
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

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.ui.model.ExtendedTableModel;

public class TableColumnSorter extends java.awt.event.MouseAdapter {

    private boolean[] columnclicked;
    
    private JTable table = null;
    private ExtendedTableModel tableModel = null;
    private TableColumnModel columnModel = null;
        
    public TableColumnSorter(JTable table, ExtendedTableModel tableModel, TableColumnModel columnModel) {
        this.table = table;
        this.tableModel = tableModel;
        this.columnModel = columnModel;
        this.columnclicked = new boolean[columnModel.getColumnCount()];
    }
    
    public void mouseClicked(java.awt.event.MouseEvent e) {
        Cyberduck.DEBUG("[TableColumnSorter] mouseClicked()");
        int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
        int column = table.convertColumnIndexToModel(viewColumn); 
        if (e.getClickCount() == 1 && column >= 0 && column <=2) {
            try {
                boolean ascending = getColumnClickedTwice(column);
                tableModel.sort(column, ascending); 
                this.setColumnClickedTwice(column);
            }
            catch(IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void setColumnClickedTwice(int column) throws IllegalArgumentException {
        if(column >= columnclicked.length) {
            throw new IllegalArgumentException("[TableColumnSorter] getColumnClickedTwice(" + column + "): Invalid parameter.");
        }
        this.columnclicked[column] = !columnclicked[column];
    }
    
    private boolean getColumnClickedTwice(int column) throws IllegalArgumentException {
        if(column >= this.columnclicked.length) {
            throw new IllegalArgumentException("[TableColumnSorter] getColumnClickedTwice(" + column + "): Invalid parameter.");
        }
        return columnclicked[column];
    }
}
