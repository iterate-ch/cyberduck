package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.Validator;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDValidatorController extends Validator {
    private static Logger log = Logger.getLogger(CDValidatorController.class);
	
	public CDValidatorController(boolean resume) {
		super(resume);
	}
	
	private NSImageView iconView; // IBOutlet
	public void setIconView(NSImageView iconView) {
		this.iconView = iconView;
	}
	
    private NSTextField alertTextField; // IBOutlet
	public void setAlertTextField(NSTextField alertTextField) {
		this.alertTextField = alertTextField;
	}
	
    private NSButton applyCheckbox; // IBOutlet
	public void setApplyCheckbox(NSButton applyCheckbox) {
		this.applyCheckbox = applyCheckbox;
	}

    private NSButton resumeButton; // IBOutlet
	public void setResumeButton(NSButton resumeButton) {
		this.resumeButton = resumeButton;
	}

	private NSWindow window; // IBOutlet
    public void setWindow(NSWindow window) {
        this.window = window;
        this.window.setDelegate(this);
    }
	
    public NSWindow window() {
        return this.window;
    }
	
	private boolean applyToAll = false;
    private boolean proceed = false;
    private boolean done = true;
	
	public boolean prompt(Path path) {
		if(false == applyToAll) {
			this.done = false;
			if (false == NSApplication.loadNibNamed("Validator", this)) {
				log.fatal("Couldn't load Validator.nib");
			}
			this.resumeButton.setEnabled(!path.status.isComplete());
			this.alertTextField.setStringValue(NSBundle.localizedString("The file", "")+" '"+path.getLocal().getAbsolutePath()+"' "+NSBundle.localizedString("already exists.", "")); // message
			NSImage img = NSWorkspace.sharedWorkspace().iconForFileType(path.getExtension());
			img.setScalesWhenResized(true);
			img.setSize(new NSSize(64f, 64f));
			this.iconView.setImage(img);
//@todo			while (!SHEET_CLOSED) {
			NSApplication.sharedApplication().beginSheet(this.window(), //sheet
														 CDQueueController.instance().window(),
														 this, //modalDelegate
														 new NSSelector("validateSheetDidEnd",
																		new Class[]{NSWindow.class, int.class, Object.class}), // did end selector
														 path); //contextInfo
			this.window().makeKeyAndOrderFront(null);
		}
		// Waiting for user to make choice
		while (!done) {
			try {
				log.debug("Sleeping...");
				Thread.sleep(1000); //milliseconds
			}
			catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
		path.status.setResume(resume);
		log.debug("return:" + proceed);
		return proceed;
	}

    public void resumeActionFired(NSButton sender) {
		log.debug("resumeActionFired");
		this.applyToAll = (applyCheckbox.state() == NSCell.OnState);
		this.resume = true;
		this.proceed = true;
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
	}

	public void overwriteActionFired(NSButton sender) {
		log.debug("overwriteActionFired");
		this.applyToAll = (applyCheckbox.state() == NSCell.OnState);
		this.resume = false;
		this.proceed = true;
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
	}
	
    public void skipActionFired(NSButton sender) {
		log.debug("skipActionFired");
		this.applyToAll = (applyCheckbox.state() == NSCell.OnState);
		this.resume = false;
		this.proceed = false;
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
	}
	
	public void cancelActionFired(NSButton sender) {
		log.debug("cancelActionFired");
		this.applyToAll = true;
		this.resume = true;
		this.proceed = false;
        NSApplication.sharedApplication().endSheet(this.window, sender.tag());
	}
	
    public void validateSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        this.window().close();
/*        Path item = (Path) contextInfo;
        switch (returncode) {
            case NSAlertPanel.DefaultReturn: //Overwrite
                item.status.setResume(false);
				this.resume = false;
                this.proceed = true;
                break;
            case NSAlertPanel.AlternateReturn: //Resume
                item.status.setResume(true);
				this.resume = true;
                this.proceed = true;
                break;
            case NSAlertPanel.OtherReturn: //Cancel
                this.proceed = false;
                break;
        }
		*/
        this.done = true;
    }
}