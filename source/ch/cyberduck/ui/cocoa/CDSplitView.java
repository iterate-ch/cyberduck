package ch.cyberduck.ui.cocoa;

/*
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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDSplitView extends NSSplitView {
    private static Logger log = Logger.getLogger(CDSplitView.class);

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------
    
    private NSTableView browserView;
    public void setBrowserView(NSTableView browserView) {
	this.browserView = browserView;
    }
    
    private NSTableView transferView;
    public void setTransferView(NSTableView transferView) {
	this.transferView = transferView;
    }

    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------
    
    public CDSplitView() {
	super();
	log.debug("CDSplitView");
    }

    public CDSplitView(NSRect frameRect) {
	super(frameRect);
	log.debug("CDSplitView");
    }

    public void awakeFromNib() {
	log.debug("awakeFromNib");
	this.setDelegate(this);
	this.setVertical(false);
	this.addSubview(browserView);
	this.addSubview(transferView);
	//Adjusts the sizes of the receiver's subviews so they (plus the dividers) fill the receiver. The subviews are resized proportionally; the size of a subview relative to the other subviews doesn't change.
	this.adjustSubviews();
    }

    // ----------------------------------------------------------
    // Delegate methods
    // ----------------------------------------------------------

    public boolean splitViewCanCollapseSubview( NSSplitView sender, NSView subview) {
	log.debug("splitViewCanCollapseSubview");
	return subview.equals(transferView);
    }

    //	Returns the thickness of the divider. You can subclass NSSplitView and override this method to change the divider's size, if necessary.
//    public float dividerThickness()

    public float splitViewConstrainMaxSplitPosition(NSSplitView sender, float proposedMax, int offset) {
	log.debug("splitViewConstrainMaxSplitPosition:"+proposedMax);
	return proposedMax;
    }

    //Allows the delegate for sender to constrain the minimum coordinate limit of a divider when the user drags it.
    //This method is invoked before the NSSplitView begins tracking the cursor to position a divider. You may further constrain the
    //limits that have been already set, but you cannot extend the divider limits. proposedMin is specified in the NSSplitView's
    //flipped coordinate system. If the split bars are horizontal (views are one on top of the other), proposedMin is the top limit. If the
    //split bars are vertical (views are side by side), proposedMin is the left limit. The initial value of proposedMin is the top
    //(or left side) of the subview before the divider. offset specifies the divider the user is moving, with the first divider being
    //0 and going up from top to bottom (or left to right).
    public float splitViewConstrainMinSplitPosition(NSSplitView sender, float proposedMin, int offset) {
	log.debug("splitViewConstrainMinSplitPosition:"+proposedMin);
	return proposedMin;
    }
}