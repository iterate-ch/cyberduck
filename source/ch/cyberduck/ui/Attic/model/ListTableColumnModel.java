package ch.cyberduck.ui.model;

/*
 *  ch.cyberduck.ui.model.ListTableColumnModel.java
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

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.Path;

/**
* @version $Id$
 */
public class ListTableColumnModel extends DefaultTableColumnModel {
    
    public static final int TYPECOLUMN  = 0;
    public static final int FILENAMECOLUMN = 1;
    public static final int SIZECOLUMN = 2;
    public static final int MODIFIEDCOLUMN = 3;
    public static final int OWNERCOLUMN = 4;
    public static final int ACCESSCOLUMN = 5;

    String[] columnNames = {" ", "Filename", "Size", "Modified", "Owner", "Access"};
    String[] propertyNames = {"ftp.listing.showType","ftp.listing.showFilenames","ftp.listing.showSize","ftp.listing.showDate","ftp.listing.showOwner","ftp.listing.showAccess"};
    
    /*
    int[] columnPrefWidths = {
        Integer.parseInt(Preferences.instance().getProperty("table.column0.width")), 
        Integer.parseInt(Preferences.instance().getProperty("table.column1.width")), 
        Integer.parseInt(Preferences.instance().getProperty("table.column2.width")), 
        Integer.parseInt(Preferences.instance().getProperty("table.column3.width")),
    };
    */
    private int[] columnPrefWidths = {20, 500, 100, 170, 100, 100};
    private int[] columnMinWidths = {20, 100, 50, 50, 50, 50};
    private int[] columnMaxWidths = {20, 5000, 150, 180, 150, 120};

    private int columnCount = columnNames.length;
    
    public ListTableColumnModel() {
        super();
        Cyberduck.DEBUG("[ListTableColumnModel] new ListTableColumnModel()");
        TableColumn column = null;
        for (int i = 0; i < columnNames.length; i++) {
            column = new TableColumn(i);
            column.setHeaderValue(columnNames[i]);
            column.setMinWidth(columnMinWidths[i]);
            column.setPreferredWidth(columnPrefWidths[i]);
            column.setMaxWidth(columnMaxWidths[i]);
            if(i == TYPECOLUMN) {
                column.setCellRenderer(new IconRenderer());
            }
            if(i == FILENAMECOLUMN) {
                column.setCellRenderer(new ToolTipRenderer());
            }
            if(Preferences.instance().getProperty(propertyNames[i]).equals("true"))
                this.addColumn(column);
            else
                columnCount--;
            
//            this.moveColumn(i, Integer.parseInt(Preferences.instance().getProperty("table.column" + i + ".position")));

        }
    }

    /*
    public TableColumn getColumn(int index) {
        TableColumn c = null;
        if(index < this.getColumnCount())
            c = super.getColumn(index);
        return c;
    }
     */
    
    public int getColumnCount() {
        return this.columnCount;
    }
    public String getColumnName(int col) {
        return this.columnNames[col];
    }

    private class ToolTipRenderer extends javax.swing.table.DefaultTableCellRenderer {
        public ToolTipRenderer() {
            super();
        }
        public java.awt.Component getTableCellRendererComponent(
                                                                javax.swing.JTable table, Object obj,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int col) {
            java.awt.Component c = super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, col);
            ListTableModel model = (ListTableModel)table.getModel();
            Path p = (Path)model.getEntry(row);
            if(p != null)
                ((javax.swing.JComponent)c).setToolTipText(p.toString());
            return c;
        }
    }
    
    private class IconRenderer extends javax.swing.JLabel implements javax.swing.table.TableCellRenderer {
        public IconRenderer() {
            super();
        }
        public java.awt.Component getTableCellRendererComponent(
                                                       javax.swing.JTable table, Object obj,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            javax.swing.JLabel label = (javax.swing.JLabel)obj;
            if (label != null) {
                ListTableModel model = (ListTableModel)table.getModel();
                Path p = (Path)model.getEntry(row);
                if(p.isDirectory()) {
                    this.setIcon(ch.cyberduck.ui.common.GUIFactory.FOLDER_ICON);
                }
                else if(p.isFile()) {
                    this.setIcon(ch.cyberduck.ui.common.GUIFactory.FILE_ICON);
                }
                else {
                    this.setIcon(ch.cyberduck.ui.common.GUIFactory.UNKNOWN_ICON);
                }
            }
            return this;
        }
    }
}
