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
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Preferences;

public class CDBrowserTable extends CDTableView {
    private static Logger log = Logger.getLogger(CDBrowserTable.class);

    public CDBrowserTable() {
        super();
    }

    public CDBrowserTable(NSRect frame) {
        super(frame);
    }

	protected CDBrowserTable(boolean a, int b) {
        super(a, b);
    }
	
    protected CDBrowserTable(NSCoder decoder, long token) {
        super(decoder, token);
    }

    protected void encodeWithCoder(NSCoder encoder) {
        super.encodeWithCoder(encoder);
    }

    public void awakeFromNib() {
        log.debug("awakeFromNib");
		// receive drag events from types
        this.registerForDraggedTypes(new NSArray(new Object[]{
			"QueuePboardType",
            NSPasteboard.FilenamesPboardType})
									 );

		// setting appearance attributes
        this.setRowHeight(17f);
        this.setAutoresizesAllColumnsToFit(true);
        NSSelector setUsesAlternatingRowBackgroundColorsSelector =
                new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
        if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
            this.setUsesAlternatingRowBackgroundColors(Preferences.instance().getProperty("browser.alternatingRows").equals("true"));
        }
        NSSelector setGridStyleMaskSelector =
                new NSSelector("setGridStyleMask", new Class[]{int.class});
        if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
            if (Preferences.instance().getProperty("browser.horizontalLines").equals("true") && Preferences.instance().getProperty("browser.verticalLines").equals("true")) {
                this.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask | NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getProperty("browser.verticalLines").equals("true")) {
                this.setGridStyleMask(NSTableView.SolidVerticalGridLineMask);
            }
            else if (Preferences.instance().getProperty("browser.horizontalLines").equals("true")) {
                this.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
            }
            else {
                this.setGridStyleMask(NSTableView.GridNone);
            }
        }

// ading table columns
        if (Preferences.instance().getProperty("browser.columnIcon").equals("true")) {
            NSTableColumn c = new NSTableColumn();
            c.setIdentifier("TYPE");
            c.headerCell().setStringValue("");
            c.setMinWidth(20f);
            c.setWidth(20f);
            c.setMaxWidth(20f);
            c.setResizable(true);
            c.setEditable(false);
            c.setDataCell(new NSImageCell());
            c.dataCell().setAlignment(NSText.CenterTextAlignment);
            this.addTableColumn(c);
        }
        if (Preferences.instance().getProperty("browser.columnFilename").equals("true")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Filename", "A column in the browser"));
            c.setIdentifier("FILENAME");
            c.setMinWidth(100f);
            c.setWidth(250f);
            c.setMaxWidth(1000f);
            c.setResizable(true);
            c.setEditable(false);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.addTableColumn(c);
        }
        if (Preferences.instance().getProperty("browser.columnSize").equals("true")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Size", "A column in the browser"));
            c.setIdentifier("SIZE");
            c.setMinWidth(50f);
            c.setWidth(80f);
            c.setMaxWidth(100f);
            c.setResizable(true);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.RightTextAlignment);
            this.addTableColumn(c);
        }
        if (Preferences.instance().getProperty("browser.columnModification").equals("true")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Modified", "A column in the browser"));
            c.setIdentifier("MODIFIED");
            c.setMinWidth(100f);
            c.setWidth(180f);
            c.setMaxWidth(500f);
            c.setResizable(true);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.addTableColumn(c);
        }
        if (Preferences.instance().getProperty("browser.columnOwner").equals("true")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Owner", "A column in the browser"));
            c.setIdentifier("OWNER");
            c.setMinWidth(100f);
            c.setWidth(80f);
            c.setMaxWidth(500f);
            c.setResizable(true);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.addTableColumn(c);
        }
        if (Preferences.instance().getProperty("browser.columnPermissions").equals("true")) {
            NSTableColumn c = new NSTableColumn();
            c.headerCell().setStringValue(NSBundle.localizedString("Permissions", "A column in the browser"));
            c.setIdentifier("PERMISSIONS");
            c.setMinWidth(100f);
            c.setWidth(100f);
            c.setMaxWidth(800f);
            c.setResizable(true);
            c.setDataCell(new NSTextFieldCell());
            c.dataCell().setAlignment(NSText.LeftTextAlignment);
            this.addTableColumn(c);
        }

        this.sizeToFit();
		// selection properties
        this.setAllowsMultipleSelection(true);
        this.setAllowsEmptySelection(true);
        this.setAllowsColumnReordering(true);
    }
	
    public void keyDown(NSEvent event) {
        String chars = event.characters();
        double timestamp = event.timestamp();
		//        CDTableDataSource model = ((CDTableDataSource) this.dataSource());
		Object ds = this.dataSource();
		if(ds instanceof CDTableDataSource) {
			CDTableDataSource model = (CDTableDataSource)ds;
			for (int i = 0; i < model.numberOfRowsInTableView(this); i++) {
				String filename = (String) model.tableViewObjectValueForLocation(this, this.tableColumnWithIdentifier("FILENAME"), i);
				if (filename.toLowerCase().startsWith(chars)) {
					this.selectRow(i, false);
					this.scrollRowToVisible(i);
//					return;
				}
			}
		}
//        this.interpretKeyEvents(new NSArray(event));
		super.keyDown(event);
    }
}
