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

public class CDTableView extends NSTableView {

    public CDTableView() {
        super();
    }

    public CDTableView(NSRect frame) {
        super(frame);
    }

    protected CDTableView(NSCoder decoder, long token) {
        super(decoder, token);
    }

    protected void encodeWithCoder(NSCoder encoder) {
        super.encodeWithCoder(encoder);
    }

	/*
	public void moveDown(NSObject sender) {
        int row = this.selectedRow();
        row++;
        if (row >= this.numberOfRows()) {
            NSApplication.beep();
            return;
        }
        this.selectRow(row, false);
        this.scrollRowToVisible(row);
    }
	 */
	
	/*
    public void moveUp(NSObject sender) {
        int row = this.selectedRow();
        row--;
        if (row < 0) {
            NSApplication.beep();
            return;
        }
        this.selectRow(row, false);
        this.scrollRowToVisible(row);
    }
	 */
	
	/*
	public void scrollPageUp(NSObject sender) {
        // Sheesh, isn't there an easier way?!
        NSRect v = this.visibleRect();
        NSPoint p = new NSPoint(v.origin().x(), v.origin().y() - v.size().height());
        this.scrollPoint(p);
    }
	 */

	/*
    public void scrollPageDown(NSObject sender) {
        NSRect v = this.visibleRect();
        NSPoint p = new NSPoint(v.origin().x(), v.origin().y() + v.size().height());
        this.scrollPoint(p);
    }
	 */
	
	/*
    public void scrollToBeginningOfDocument(NSObject sender) {
        this.scrollRowToVisible(0);
    }
	 */
    
	/*
    public void scrollToEndOfDocument(NSObject sender) {
        this.scrollRowToVisible(this.numberOfRows() - 1);
    }
	 */
}