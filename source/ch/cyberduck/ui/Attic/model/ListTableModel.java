package ch.cyberduck.ui.model;

/*
 *  ch.cyberduck.ui.model.ListTableModel.java
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

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.*;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.connection.Path;
import ch.cyberduck.connection.TransferAction;
import ch.cyberduck.ui.common.GUIFactory;

/**
 * The model of a directory listing
 * @version $Id$
  */
public class ListTableModel extends AbstractTableModel implements TableModel, ExtendedTableModel {
    
    private List data;
    
    public ListTableModel() {
        super();
        this.data = new ArrayList();
    }

    /**
        * @return An iterator over the table data
     */
    public Iterator iterator() {
        return data.iterator();
    }
    
    public boolean contains(Object o) {
        return data.contains(o);
    }

    public void setData(List d) {
        this.data = d;
        this.fireTableDataChanged();
    }

    /**
     * Removes all entries in this model
     */
    public void clear() {
        this.data.clear();
        this.fireTableDataChanged();
    }

    public int getColumnCount() {
        return -1;
    }

    public int getRowCount() {
        if(this.data != null)
            return this.data.size();
        return 0;
    }
    
    public void addEntry(Object newEntry, int atRow) {
        if(atRow >= this.getRowCount()) {
            this.addEntry(newEntry);
            this.fireTableRowsUpdated(this.getRowCount()-1, this.getRowCount()-1);
        }
        else {
            data.add(atRow, newEntry);
            this.fireTableRowsUpdated(atRow, atRow);
        }
    }

    public void addEntry(Object newEntry) {
        if(newEntry instanceof Path) {
            data.add(newEntry);
            this.fireTableRowsUpdated(this.getRowCount() - 1, this.getRowCount() - 1);
        }
    }

    /**
     * @param row The table row
     * @return The Object at this row
     */
    public Object getEntry(int row) {
        if(row >= this.getRowCount())
           return null;
	return data.get(row);
    }
    
    public Object getValueAt(int row, int col) {
        if(row < this.getRowCount()) {
            Path p = (Path)data.get(row);
            switch(col) {
                case ListTableColumnModel.TYPECOLUMN:
                    if(p.isDirectory()) {
                        return GUIFactory.FOLDER_LABEL;
                    }
                    else if(p.isFile()) {
                        return GUIFactory.FILE_LABEL;
                    }
                    else {
                        return GUIFactory.UNKNOWN_LABEL;
                    }
                case ListTableColumnModel.FILENAMECOLUMN:
                    return p.getName();
                case ListTableColumnModel.SIZECOLUMN:
                    return p.getSize();
                case ListTableColumnModel.OWNERCOLUMN:
                    return p.getOwner();
                case ListTableColumnModel.MODIFIEDCOLUMN:
                    return p.getModified();
                case ListTableColumnModel.ACCESSCOLUMN:
                    return p.getMode();
            }
        }
        return null;
    }

    public void setValueAt(Object input, int row, int col) {
    /*
        if(col == ListTableColumnModel.FILENAMECOLUMN) {
            Path from  = (Path)this.getEntry(row);
            Path to = new Path(from.getParent().getPath(), (String)input);
            if(!(from.getPath().equals(to.getPath()))) {
				bookmark.transfer(new TransferAction(TransferAction.RNFR, from, to));
			}
        }
		fireTableCellUpdated(row, col);
        */
    }

    public static final int TYPECOLUMN  = 0;
    public static final int FILENAMECOLUMN = 1;
    public static final int SIZECOLUMN = 2;
    public static final int MODIFIEDCOLUMN = 3;
    public static final int OWNERCOLUMN = 4;
    public static final int ACCESSCOLUMN = 5;
    
    public Class getColumnClass(int c) {
        try {
            switch(c) {
                case ListTableColumnModel.TYPECOLUMN:
                    return Class.forName("java.lang.JLabel");
                case ListTableColumnModel.FILENAMECOLUMN:
                    return Class.forName("java.lang.String");
                case ListTableColumnModel.SIZECOLUMN:
                    return Class.forName("java.lang.Integer");
                case ListTableColumnModel.MODIFIEDCOLUMN:
                    return Class.forName("java.lang.String");
                case ListTableColumnModel.OWNERCOLUMN:
                    return Class.forName("java.lang.String");
                case ListTableColumnModel.ACCESSCOLUMN:
                    return Class.forName("java.lang.String");
                default:
                    throw new IllegalArgumentException("No such column: " + c);
            }

        }
        catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return always false
     */
    public boolean isCellEditable(int row, int col) {
    	/*
        if(col == ListTableColumnModel.FILENAMECOLUMN)
        	return true;
        	*/
        return false;
    }

    /*
     * @param columnIndex The column to sort
     * @param ascending Alphabetically or reverse
     */
    public void sort(final int columnIndex, final boolean ascending) {
        Cyberduck.DEBUG("[ListTableModel] sort(" + columnIndex + ", " + ascending + ")");
        final int higher;
        final int lower;
        if(ascending) {
            higher = 1;
            lower = -1;
        }
        else {
            higher = -1;
            lower = 1;
        }
        switch(columnIndex) {
            case ListTableColumnModel.TYPECOLUMN:
                Collections.sort(data,
                                 new Comparator() {
                                     public int compare(Object o1, Object o2) {
                                         Path p1 = (Path) o1;
                                         Path p2 = (Path) o2;
                                         if(p1.isDirectory() && p2.isDirectory())
                                             return 0;
                                         if(p1.isFile() && p2.isFile())
                                             return 0;
                                         if(p1.isFile())
                                             return higher;
                                         return lower;
                                     }
                                 }
                                 );
                break;
            case ListTableColumnModel.FILENAMECOLUMN:
                Collections.sort(data,
                                 new Comparator() {
                                     public int compare(Object o1, Object o2) {
                                         Path p1 = (Path) o1;
                                         Path p2 = (Path) o2;
                                         return p1.toString().compareTo(p2.toString());
                                     }
                                 }
                                 );
                break;
            case ListTableColumnModel.SIZECOLUMN:
                Collections.sort(data,
                                 new Comparator() {
                                     public int compare(Object o1, Object o2) {
                                         Path p1 = (Path) o1;
                                         Path p2 = (Path) o2;
                                         return p1.toString().compareTo(p2.toString());
                                     }
                                 }
                                 );
            case ListTableColumnModel.MODIFIEDCOLUMN:
                Collections.sort(data,
                                 new Comparator() {
                                     public int compare(Object o1, Object o2) {
                                         Path p1 = (Path) o1;
                                         Path p2 = (Path) o2;
                                         return p1.getModified().compareTo(p2.getModified());
                                     }
                                 }
                                 );
                break;
            case ListTableColumnModel.OWNERCOLUMN:
                Collections.sort(data,
                                 new Comparator() {
                                     public int compare(Object o1, Object o2) {
                                         Path p1 = (Path) o1;
                                         Path p2 = (Path) o2;
                                         return p1.getOwner().compareTo(p2.getOwner());
                                     }
                                 }
                                 );
                break;
            case ListTableColumnModel.ACCESSCOLUMN:
                Collections.sort(data,
                                 new Comparator() {
                                     public int compare(Object o1, Object o2) {
                                         Path p1 = (Path) o1;
                                         Path p2 = (Path) o2;
                                         return p1.getMode().compareTo(p2.getMode());
                                     }
                                 }
                                 );
                break;
            default:
                break;
        }
    }
}
