/*
 *  ch.cyberduck.ui.cocoa.CDSplitView.java
 *  Cyberduck
 *
 *  Copyright (c) 2002 David Kocher. All rights reserved.
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
package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDSplitView extends NSSplitView {

    private static Logger log = Logger.getLogger(CDSplitView.class);

    public NSView browserView;
    public NSView transferView;
    
    public CDSplitView() {
	super();
	log.debug("CDSplitView");
    }

    public CDSplitView(NSRect frameRect) {
	super(frameRect);
	log.debug("CDSplitView");
    }

    public boolean splitViewCanCollapseSubview( NSSplitView sender, NSView subview) {
	log.debug("CDSplitView:splitViewCanCollapseSubview");
	return true;
    }
    
    public boolean canCollapseSubview(NSView subview) {
	log.debug("CDSplitView:canCollapseSubview");
	return (subview == transferView);
    }

    public void awakeFromNib() {
	log.debug("CDSplitView:awakeFromNib");
	this.setDelegate(this);
	this.setVertical(false);
	this.addSubview(browserView);
	this.addSubview(transferView);
	//Adjusts the sizes of the receiver's subviews so they (plus the dividers) fill the receiver. The subviews are resized proportionally; the size of a subview relative to the other subviews doesn't change.
	this.adjustSubviews();
    }

    //	Returns the thickness of the divider. You can subclass NSSplitView and override this method to change the divider's size, if necessary.
//    public float dividerThickness()

	    
}
