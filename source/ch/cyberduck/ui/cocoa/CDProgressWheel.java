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

import java.util.Observer;
import java.util.Observable;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Status;
import ch.cyberduck.core.Message;

/**
* @version $Id$
 */
public class CDProgressWheel extends NSProgressIndicator implements Observer {

    private static Logger log = Logger.getLogger(CDProgressWheel.class);

    public CDProgressWheel() {
	super();
    }
    
    public CDProgressWheel(NSRect frame) {
	super(frame);
    }

    public void update(Observable o, Object arg) {
//	log.debug("update:"+arg);
	if(o instanceof Status) {
	    Message msg = (Message)arg;
	    if(msg.getTitle().equals(Message.ACTIVE)) {
		this.startAnimation(this);
	    }
	    if(msg.getTitle().equals(Message.STOP)) {
		this.stopAnimation(this);
	    }
	}
    }

    public CDProgressWheel(NSCoder decoder, long token) {
	super(decoder, token);
	log.debug("CDProgressWheel");
    }

    public void encodeWithCoder(NSCoder encoder) {
	super.encodeWithCoder(encoder);
    }
}
