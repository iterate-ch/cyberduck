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

/**
 * @version $Id$
 */
public abstract class CDController {
	protected static Logger log = Logger.getLogger(CDController.class);

	private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();
	
	static {
		lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
	}
	
	protected static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingMiddleParagraph},
																	  new Object[]{NSAttributedString.ParagraphStyleAttributeName});
	
	protected void finalize() throws Throwable {
		log.debug("------------- finalize:"+this.toString());
		super.finalize();
	}

	private NSWindow sheet; // IBOutlet

	public void setWindow(NSWindow window) {
		this.sheet = window;
		this.sheet.setDelegate(this);
	}

	public NSWindow window() {
		return this.sheet;
	}

	public abstract void awakeFromNib();

	public boolean windowShouldClose(NSWindow sender) {
		return true;
	}

	public abstract void windowWillClose(NSNotification notification);

	public void cascade() {
		NSArray windows = NSApplication.sharedApplication().windows();
		int count = windows.count();
		if(count != 0) {
			while(0 != count--) {
				NSWindow window = (NSWindow)windows.objectAtIndex(count);
				NSPoint origin = window.frame().origin();
				origin = new NSPoint(origin.x(), origin.y()+window.frame().size().height());
				this.window().setFrameTopLeftPoint(this.window().cascadeTopLeftFromPoint(origin));
				break;
			}
		}
    }

    public void endSheet(NSWindow sheet, int tag) {
        log.debug("endSheet");
        if(modalSession != null) {
            NSApplication.sharedApplication().endModalSession(modalSession);
            modalSession = null;
        }
        NSApplication.sharedApplication().endSheet(sheet, tag);
        sheet.orderOut(null);
        synchronized(this) {
            this.notifyAll();
        }
    }

    public void waitForSheetEnd() {
        log.debug("waitForSheetEnd");
        synchronized(this) {
            while(this.hasSheet()) {
                try {
                    if(Thread.currentThread().getName().equals("main")) {
                        return;
                    }
                    log.debug("Sleeping:waitForSheetEnd...");
                    this.wait();
                    log.debug("Awakened:waitForSheetEnd");
                }
                catch(InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public void waitForSheetDisplay(NSWindow sheet) {
        log.debug("waitForSheetDisplay:"+sheet);
        synchronized(this) {
            while(this.window().attachedSheet() != sheet) {
                try {
                    if(Thread.currentThread().getName().equals("main")) {
                        return;
                    }
                    log.debug("Sleeping:waitForSheetDisplay...");
                    this.wait();
                    log.debug("Awakened:waitForSheetDisplay");
                }
                catch(InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    public void sheetDidClose(NSWindow sheet, int returncode, Object contextInfo) {
        this.endSheet(sheet, returncode);
    }

    public void beginSheet(NSWindow sheet) {
        this.beginSheet(sheet, new NSSelector
                ("sheetDidClose",
                        new Class[]
                        {
                            NSWindow.class, int.class, Object.class
                        })); // end selector);
    }

    public void beginSheet(NSWindow sheet, NSSelector endSelector) {
        this.beginSheet(sheet, endSelector, null);
    }

    public void beginSheet(NSWindow sheet, NSSelector endSelector, Object contextInfo) {
        this.beginSheet(sheet, this, endSelector, contextInfo);
    }

    public void beginSheet(final NSWindow sheet, final Object delegate, final NSSelector endSelector, final Object contextInfo) {
        log.debug("beginSheet:"+sheet);
        synchronized(this) {
            this.waitForSheetEnd();
            NSApplication app = NSApplication.sharedApplication();
            app.beginSheet(sheet, //sheet
                    this.window(),
                    delegate, //modalDelegate
                    endSelector, // did end selector
                    contextInfo); //contextInfo
            sheet.makeKeyAndOrderFront(null);
            if(Thread.currentThread().getName().equals("main")) {
                log.warn("Waiting on main thread; will run modal!");
                modalSession = NSApplication.sharedApplication().beginModalSessionForWindow(sheet);
                while(this.hasSheet()) {
                    app.runModalSession(modalSession);
                }
            }
            this.notifyAll();
        }
    }

    private NSModalSession modalSession = null;

	public boolean hasSheet() {
		return this.window().attachedSheet() != null;
	}
}