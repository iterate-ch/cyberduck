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

import ch.cyberduck.core.QueueList;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Preferences;

public class CDQueueTable extends CDTableView {
    private static Logger log = Logger.getLogger(CDQueueTable.class);
	
    public CDQueueTable() {
        super();
    }
	
    public CDQueueTable(NSRect frame) {
        super(frame);
    }
	
	protected CDQueueTable(boolean a, int b) {
        super(a, b);
    }
	
    protected CDQueueTable(NSCoder decoder, long token) {
        super(decoder, token);
    }
	
    protected void encodeWithCoder(NSCoder encoder) {
        super.encodeWithCoder(encoder);
    }
	
	public void reloadData() {
		super.reloadData();
		QueueList.instance().save();
	}
	
    public void awakeFromNib() {
        log.debug("awakeFromNib");
		
        // receive drag events from types
        // in fact we are not interested in file promises, but because the browser model can only initiate
        // a drag with tableView.dragPromisedFilesOfTypes(), we listens for those events
        // and then use the private pasteboard instead.
        this.registerForDraggedTypes(new NSArray(new Object[]{"QueuePBoardType",
			NSPasteboard.StringPboardType,
			NSPasteboard.FilesPromisePboardType}));
		
        this.setRowHeight(50f);
		
        NSTableColumn dataColumn = new NSTableColumn();
        dataColumn.setIdentifier("DATA");
        dataColumn.setMinWidth(200f);
        dataColumn.setWidth(350f);
        dataColumn.setMaxWidth(1000f);
        dataColumn.setEditable(false);
        dataColumn.setResizable(true);
        dataColumn.setDataCell(new CDQueueCell());
        this.addTableColumn(dataColumn);
		
        NSTableColumn progressColumn = new NSTableColumn();
        progressColumn.setIdentifier("PROGRESS");
        progressColumn.setMinWidth(80f);
        progressColumn.setWidth(300f);
        progressColumn.setMaxWidth(1000f);
        progressColumn.setEditable(false);
        progressColumn.setResizable(true);
        progressColumn.setDataCell(new CDProgressCell());
        this.addTableColumn(progressColumn);
		
        NSSelector setUsesAlternatingRowBackgroundColorsSelector =
			new NSSelector("setUsesAlternatingRowBackgroundColors", new Class[]{boolean.class});
        if (setUsesAlternatingRowBackgroundColorsSelector.implementedByClass(NSTableView.class)) {
            this.setUsesAlternatingRowBackgroundColors(true);
        }
        NSSelector setGridStyleMaskSelector =
			new NSSelector("setGridStyleMask", new Class[]{int.class});
        if (setGridStyleMaskSelector.implementedByClass(NSTableView.class)) {
            this.setGridStyleMask(NSTableView.SolidHorizontalGridLineMask);
        }
				
        //selection properties
        this.setAllowsMultipleSelection(true);
        this.setAllowsEmptySelection(true);
        this.setAllowsColumnReordering(false);

		this.sizeToFit();
	}
}
