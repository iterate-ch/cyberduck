package ch.cyberduck.ui.model;

/*
 *  ch.cyberduck.ui.model.BookmarkTableModel.java
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

import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.net.URL;
import java.util.*;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Bookmark;
import ch.cyberduck.connection.Status;
import ch.cyberduck.ui.common.GUIFactory;

/**
 * The model of the bookmark table view
 * @version $Id$
  */
public class BookmarkTableModel extends AbstractTableModel implements TableModel, ExtendedTableModel {

    private List data;

    public BookmarkTableModel() {
        super();
        this.data = new ArrayList();
    }

    public void setData(List data) {
        Cyberduck.DEBUG("[BookmarkTableModel] setData()");
        this.data = data;
        this.fireTableDataChanged();
    }

    public void clear() {
        Cyberduck.DEBUG("[BookmarkTableModel] clear(()");
        Iterator iterator = data.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            ((Bookmark)iterator.next()).cleanup();
            iterator.remove();
            this.fireTableRowsDeleted(i, i); i++;
        }
    }
    
    public int getRowCount() {
        //Cyberduck.DEBUG("[BookmarkTableModel] getRowCount()");
        if(this.data != null)
            return this.data.size();
        return 0;
    }

    public Object getValueAt(int row, int col) {
        //Cyberduck.DEBUG("[BookmarkTableModel] getValueAt(" + row + ", " + col + ")");
	if (row < this.getRowCount()) {
            Bookmark bookmark = (Bookmark)data.get(row);
            switch(col) {
                case BookmarkTableColumnModel.ADDRESSCOLUMN:
//                    String address = bookmark.getAddress();
		    URL address = bookmark.getAddress();
                    if(address != null) {
                        return address.toString();
                    }
                    break;
                    
                case BookmarkTableColumnModel.STATUSCOLUMN:
                    if(bookmark.status.isComplete()) {
                        return GUIFactory.GREEN_LABEL;
                    }
                    else if(!bookmark.status.isStopped()) {
                        return GUIFactory.RED_LABEL;
                    }
                    else {
                        return GUIFactory.GRAY_LABEL;
                    }
                case BookmarkTableColumnModel.TYPECOLUMN:
                    if(bookmark.isListing()) {
                        return GUIFactory.FOLDER_LABEL;
                    }
                    else if(bookmark.isDownload()) {
                        return GUIFactory.FILE_LABEL;
                    }
                    else {
                        return GUIFactory.UNKNOWN_LABEL;
                    }
                case BookmarkTableColumnModel.PROGRESSCOLUMN:
                    return new JProgressBar(bookmark.status.getProgressModel());
            }
        }
        return null;
    }

    public void setValueAt(Object value, int row, int col) {
        Cyberduck.DEBUG("[BookmarkTableModel] setValueAt(" + row + ", " + col + ")");
        if(value != null && row < this.getRowCount()) {
            Bookmark bookmark = (Bookmark)data.get(row);
            switch(col) {
                case BookmarkTableColumnModel.ADDRESSCOLUMN:
                    bookmark.setAddress((String)value);
                    fireTableCellUpdated(row, col);
                    break;
                default :
                    fireTableCellUpdated(row, col);
                    break;
            }
        }
    }

    public Class getColumnClass(int c) {
        Cyberduck.DEBUG("[BookmarkTableModel] getColumnClass(" + c + ")");
        try {
            switch(c) {
            case BookmarkTableColumnModel.ADDRESSCOLUMN:
                return Class.forName("java.lang.String");
            case BookmarkTableColumnModel.PROGRESSCOLUMN:
                return Class.forName("javax.swing.JProgressBar");            
            case BookmarkTableColumnModel.STATUSCOLUMN:
                return Class.forName("javax.swing.JLabel");
            case BookmarkTableColumnModel.TYPECOLUMN:
                return Class.forName("javax.swing.JLabel");
            default:
                throw new IllegalArgumentException("No such column: " + c);
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isCellEditable(int row, int col) {
        if (col == BookmarkTableColumnModel.ADDRESSCOLUMN)
            return true;
        else
            return false;
    }

    public void addEntry(Object newEntry) {
        Cyberduck.DEBUG("[BookmarkTableModel] addEntry("+newEntry.toString()+")");
        this.addEntry(newEntry, this.getRowCount());
    }
    
    public void addEntry(Object newEntry, int atRow) {
        Cyberduck.DEBUG("[BookmarkTableModel] addEntry("+newEntry.toString()+")");
        if(newEntry instanceof Bookmark) {
            try {
                this.data.add(atRow, newEntry);
                this.fireTableRowsUpdated(0, this.getRowCount() - 1);
            }
            catch(IndexOutOfBoundsException e) {
                this.addEntry(newEntry);
            }
        }
    }

    public boolean contains(Object o) {
        return data.contains(o);
    }
    
    public Object getEntry(int row) {
        //Cyberduck.DEBUG("[BookmarkTableModel] getEntry(" + row + ")");
        return this.data.get(row);
    }
        
    public void deleteSelected(int[] selected) {
        Iterator iterator = data.iterator();
        int firstIndex = selected[0];
        int lastIndex = selected[selected.length - 1];
        int i = 0;
        // vorwŠrtspulen bis zum index direkt unter dem ersten zu lšschenden
        while (iterator.hasNext() && i < firstIndex) {
            iterator.next();
            i++;
        }
        // das naechste element loeschen bis und mit lastindex
        while (iterator.hasNext() && i <= lastIndex) {
            ((Bookmark)iterator.next()).cleanup();
            iterator.remove();
            this.fireTableRowsDeleted(i, i);
            i++;
        }
    }
    
    public void deleteCompleted() {
        Iterator iterator = data.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Bookmark bookmark = (Bookmark)iterator.next();
            if(bookmark.status.isComplete()) {
                bookmark.cleanup();
                iterator.remove();
                this.fireTableRowsDeleted(i, i);
                i++;
            }
        }
    }

    public Iterator iterator() {
        return data.iterator();
    }
        
    public void sort(final int columnIndex, final boolean ascending) {
        Cyberduck.DEBUG("[BookmarkTableModel] sort(" + columnIndex + ", " + ascending + ")");
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
            // address column
            case BookmarkTableColumnModel.ADDRESSCOLUMN:
                Collections.sort(data,
                    new Comparator() {
                        public int compare(Object o1, Object o2) {
                            Bookmark d1 = (Bookmark) o1;
                            Bookmark d2 = (Bookmark) o2;
                            if(d1.getAddress() == null || d2.getAddress() == null) {
                                if(d1.getAddress() == null)
                                    return higher;
                                if(d2.getAddress() == null)
                                    return lower;
                                return 0;
                            }
                            String a1 = d1.getAddress().toString();
                            String a2 = d2.getAddress().toString();
                            if(ascending) {
                                return a1.compareTo(a2);
                            }
                            else {
                                return -a1.compareTo(a2);
                            }
                        }
                    }
                );
                break;
            // label column
            case BookmarkTableColumnModel.STATUSCOLUMN:
                Collections.sort(data,
                    new Comparator() {
                        public int compare(Object o1, Object o2) {
                            Bookmark d1 = (Bookmark) o1;
                            Bookmark d2 = (Bookmark) o2;
                            Status s1 = d1.status;
                            Status s2 = d2.status;
                            if(s1.isComplete() && s2.isComplete()) {
                                return 0;
                            }
                            else if(!s1.isStopped()) {
                                return higher;
                            }
                            else if(!s2.isStopped()) {
                                return lower;
                            }
                            else if(s1.isComplete()) {
                                return higher;
                            }
                            else if(s2.isComplete()) {
                                return lower;
                            }
                            return 0;
                        }
                    }
                );
                break;
            // type column
            case BookmarkTableColumnModel.TYPECOLUMN:
                Collections.sort(data,
                    new Comparator() {
                        public int compare(Object o1, Object o2) {
                            Bookmark d1 = (Bookmark) o1;
                            Bookmark d2 = (Bookmark) o2;
                            if(d1.isListing() && d2.isListing()) {
                                return 0;
                            }
                            else if(d1.isDownload() && d2.isDownload()) {
                                return 0;
                            }
                            else if(d1.isListing()) {
                                return lower;
                            }
                            else if(d2.isListing()) {
                                return higher;
                            }
                            return 0;
                        }
                    }
                );
            // progress column
            case BookmarkTableColumnModel.PROGRESSCOLUMN:
                Collections.sort(data,
                    new Comparator() {
                        public int compare(Object o1, Object o2) {
                            Bookmark d1 = (Bookmark) o1;
                            Bookmark d2 = (Bookmark) o2;
                            int p1 = d1.status.getCurrent();
                            int p2 = d2.status.getCurrent();
                            if (p1 > p2) {
                                return lower;
                            }
                            else if (p1 < p2) {
                                return higher;
                            }
                            else if (p1 == p2) {
                                return 0;
                            }
                            return 0;
                        }
                    }
                );
                break;
            default:
                break;
        }
    }
    
    public int getColumnCount() {
        Cyberduck.DEBUG("[BookmarkTableModel] getColumnCount()");
        return -1;
    }
}    
