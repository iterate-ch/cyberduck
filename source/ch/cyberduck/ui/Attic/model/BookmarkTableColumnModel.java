package ch.cyberduck.ui.model;

/*
 *  ch.cyberduck.ui.model.TableColumnModel.java
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
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.connection.Bookmark;

/**
* @version $Id$
 */
public class BookmarkTableColumnModel extends DefaultTableColumnModel {

    public static final int STATUSCOLUMN = 0;
    public static final int ADDRESSCOLUMN = 1;
    public static final int TYPECOLUMN = 2;
    public static final int PROGRESSCOLUMN = 3;

    String[] columnNames = {" ", "Address", " ", "Progress"};
    int[] columnPrefWidths = {
        Integer.parseInt(ch.cyberduck.Preferences.instance().getProperty("table.column0.width")),
        Integer.parseInt(ch.cyberduck.Preferences.instance().getProperty("table.column1.width")),
        Integer.parseInt(ch.cyberduck.Preferences.instance().getProperty("table.column2.width")),
        Integer.parseInt(ch.cyberduck.Preferences.instance().getProperty("table.column3.width")),
    };
    int[] columnMinWidths = {12, 100, 20, 60};
    int[] columnMaxWidths = {12, 1000, 20, 200};

    public BookmarkTableColumnModel() {
        Cyberduck.DEBUG("[BookmarkTableColumnModel]");
        javax.swing.table.TableColumn column = null;
        for (int i = 0; i < columnNames.length; i++) {
            column = new javax.swing.table.TableColumn(i);
            column.setHeaderValue(columnNames[i]);
            column.setMinWidth(columnMinWidths[i]);
            column.setPreferredWidth(columnPrefWidths[i]);
            column.setMaxWidth(columnMaxWidths[i]);
            if(i == ADDRESSCOLUMN) {
                column.setCellRenderer(new URLRenderer());
            }
            if(i == PROGRESSCOLUMN) {
                column.setCellRenderer(new ProgressBarRenderer());
            }
            if(i == STATUSCOLUMN) {
                column.setCellRenderer(new StatusRenderer());
            }
            if(i == TYPECOLUMN) {
                column.setCellRenderer(new IconRenderer());
            }
            this.addColumn(column);
//            this.moveColumn(i, Integer.parseInt(Preferences.instance().getProperty("table.column" + i + ".position")));

        }
        setColumnSelectionAllowed(true);
    }

    public int getColumnCount() {
        //Cyberduck.DEBUG("[BookmarkTableColumnModel] getColumnCount()");
        return columnNames.length;
    }
    public String getColumnName(int col) {
        //Cyberduck.DEBUG("[BookmarkTableColumnModel] getColumnName(" + col + ")");
        return columnNames[col];
    }

    private class IconRenderer extends javax.swing.JLabel implements TableCellRenderer {
        public IconRenderer() {
            super();
        }
        public java.awt.Component getTableCellRendererComponent(
                                                                javax.swing.JTable table, Object obj,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int col) {
            javax.swing.JLabel label = (javax.swing.JLabel)obj;
            if (label != null) {
                BookmarkTableModel model = (BookmarkTableModel)table.getModel();
                Bookmark bookmark = (Bookmark)model.getEntry(row);
                if(bookmark.isListing()) {
                    this.setIcon(ch.cyberduck.ui.common.GUIFactory.FOLDER_ICON);
                }
                else if(bookmark.isDownload()) {
                    this.setIcon(ch.cyberduck.ui.common.GUIFactory.FILE_ICON);
                }
                else {
                    this.setIcon(ch.cyberduck.ui.common.GUIFactory.UNKNOWN_ICON);
                }
                processFocusEvent(new java.awt.event.FocusEvent(this, hasFocus ? java.awt.event.FocusEvent.FOCUS_GAINED
                                                                : java.awt.event.FocusEvent.FOCUS_LOST, true));
            }
            return this;
        }
    }

    private class URLRenderer extends javax.swing.table.DefaultTableCellRenderer implements TableCellRenderer {
        public URLRenderer() {
            super();
        }
        public java.awt.Component getTableCellRendererComponent(
                                                                javax.swing.JTable table, Object obj,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int col) {
            javax.swing.JLabel c = (javax.swing.JLabel)super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, col);
            if (c != null) {
                BookmarkTableModel model = (BookmarkTableModel)table.getModel();
                Bookmark bookmark = (Bookmark)model.getEntry(row);
                c.setToolTipText(bookmark.getDescription());
                if(hasFocus)
                    c.setText(bookmark.getURLAsString());
                else
                    c.setText(bookmark.getAddressAsString());
            }
            return c;
        }
    }

    private class ProgressBarRenderer extends javax.swing.JProgressBar implements TableCellRenderer {
        public ProgressBarRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(
                                                       javax.swing.JTable table, Object obj,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            javax.swing.JProgressBar progress = (javax.swing.JProgressBar)obj;
            if (progress != null) {
                BookmarkTableModel model = (BookmarkTableModel)table.getModel();
                Bookmark t = (Bookmark)model.getEntry(row);
                this.setModel(t.status.getProgressModel());
            }
            return this;
        }
    }

    private class StatusRenderer extends javax.swing.JLabel implements TableCellRenderer {
        public StatusRenderer() {
            super();
        }
        public java.awt.Component getTableCellRendererComponent(
                                                                javax.swing.JTable table, Object obj,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int col) {
            javax.swing.JLabel label = (javax.swing.JLabel)obj;
            if (label != null) {
                BookmarkTableModel model = (BookmarkTableModel)table.getModel();
                Bookmark bookmark = (Bookmark)model.getEntry(row);
                this.setIcon(bookmark.status.getIcon());
            }
            return this;
        }
    }
}
