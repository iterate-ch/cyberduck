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

package ch.cyberduck.ui.cocoa;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;
import org.apache.log4j.Logger;
import ch.cyberduck.core.Path;

/**
* @version $Id$
 */
public class CDInfoWindow extends NSPanel {

    public NSTextField filenameField; /* IBOutlet */
    public NSTextField groupField; /* IBOutlet */
    public NSTextField kindField; /* IBOutlet */
    public NSTextField modifiedField; /* IBOutlet */
    public NSTextField ownerField; /* IBOutlet */
    public NSTextField sizeField; /* IBOutlet */

    private Path selectedPath;

    private static Logger log = Logger.getLogger(CDInfoWindow.class);

    public CDInfoWindow() {
	super();
	log.debug("CDInfoWindow");
    }

    public CDInfoWindow(NSRect contentRect, int styleMask, int backingType, boolean defer) {
	super(contentRect, styleMask, backingType, defer);
	log.debug("CDInfoWindow");
    }

    public CDInfoWindow(NSRect contentRect, int styleMask, int bufferingType, boolean defer, NSScreen aScreen) {
	super(contentRect, styleMask, bufferingType, defer, aScreen);
	log.debug("CDInfoWindow");
    }

    public void awakeFromNib() {
	log.debug("CDInfoWindow:awakeFromNib");
    }

    public void setSelectedPath(Path p) {
	this.selectedPath = p;
    }
}
