package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSCoder;
import com.apple.cocoa.foundation.NSRect;
import com.apple.cocoa.foundation.NSSelector;

import org.apache.log4j.Logger;

import ch.cyberduck.core.BookmarkList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Preferences;

public class CDBookmarkTable extends NSTableView {
    private static Logger log = Logger.getLogger(CDBookmarkTable.class);

    public CDBookmarkTable() {
        super();
    }

    public CDBookmarkTable(NSRect frame) {
        super(frame);
    }

    protected CDBookmarkTable(NSCoder decoder, long token) {
        super(decoder, token);
    }

    protected void encodeWithCoder(NSCoder encoder) {
        super.encodeWithCoder(encoder);
    }
	
	public void reloadData() {
		super.reloadData();
		BookmarkList.instance().save();
	}

    public void awakeFromNib() {
        log.debug("awakeFromNib");
// receive drag events from types
        this.registerForDraggedTypes(new NSArray(new Object[]{"BookmarkPboardType",
                                                              NSPasteboard.FilenamesPboardType, //accept bookmark files dragged from the Finder
                                                              NSPasteboard.FilesPromisePboardType} //accept file promises made myself but then interpret them as BookmarkPboardType
        ));
        this.setRowHeight(45f);

        NSTableColumn iconColumn = new NSTableColumn();
        iconColumn.setIdentifier("ICON");
        iconColumn.setMinWidth(32f);
        iconColumn.setWidth(32f);
        iconColumn.setMaxWidth(32f);
        iconColumn.setEditable(false);
        iconColumn.setResizable(true);
        iconColumn.setDataCell(new NSImageCell());
        this.addTableColumn(iconColumn);

        NSTableColumn bookmarkColumn = new NSTableColumn();
        bookmarkColumn.setIdentifier("BOOKMARK");
        bookmarkColumn.setMinWidth(50f);
        bookmarkColumn.setWidth(200f);
        bookmarkColumn.setMaxWidth(500f);
        bookmarkColumn.setEditable(false);
        bookmarkColumn.setResizable(true);
        bookmarkColumn.setDataCell(new CDBookmarkCell());
        this.addTableColumn(bookmarkColumn);

		// setting appearance attributes
        this.setAutoresizesAllColumnsToFit(true);
        NSSelector setUsesAlternatingRowBackgroundColorsSelector =
                new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
        if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
            this.setUsesAlternatingRowBackgroundColors(Preferences.instance().getProperty("browser.alternatingRows").equals("true"));
        }
        NSSelector setGridStyleMaskSelector =
                new NSSelector("setGridStyleMask", new Class[]{int.class});
        if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
            this.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
        }
        this.setAutoresizesAllColumnsToFit(true);

		// selection properties
        this.setAllowsMultipleSelection(false);
        this.setAllowsEmptySelection(true);
        this.setAllowsColumnReordering(false);

        this.sizeToFit();
    }

    public void keyDown(NSEvent event) {
        String chars = event.characters();
        double timestamp = event.timestamp();
        CDTableDataSource model = ((CDTableDataSource) this.dataSource());
        for (int i = 0; i < model.numberOfRowsInTableView(this); i++) {
            Host h = (Host) model.tableViewObjectValueForLocation(this, this.tableColumnWithIdentifier("BOOKMARK"), i);
            if (h.getNickname().toLowerCase().startsWith(chars)) {
                this.selectRow(i, false);
                this.scrollRowToVisible(i);
                return;
            }
        }
		super.keyDown(event);
//        this.interpretKeyEvents(new NSArray(event));
    }
}
