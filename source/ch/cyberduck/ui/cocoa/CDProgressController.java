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

import ch.cyberduck.core.Message;
import ch.cyberduck.core.Queue;

import java.util.Observer;
import java.util.Observable;

import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class CDProgressController extends NSObject implements Observer {
    private static Logger log = Logger.getLogger(CDProgressController.class);
	
	private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();
	
    static {
        lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
    }
	
    private static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingMiddleParagraph},
																							  new Object[]{NSAttributedString.ParagraphStyleAttributeName});
	
	private Queue queue;
	
	public CDProgressController(Queue queue) {
		this.queue = queue;
		this.queue.addObserver(this);
        if (false == NSApplication.loadNibNamed("Progress", this)) {
            log.fatal("Couldn't load Progress.nib");
        }
    }
	
    public void update(Observable o, Object arg) {
//		log.debug("update:"+arg);
		if (arg instanceof Message) {
			Message msg = (Message)arg;
			if (msg.getTitle().equals(Message.DATA)) {
				this.updateProgressbar();
				this.updateProgressfield();
			}
			else if (msg.getTitle().equals(Message.PROGRESS)) {
				this.updateProgressfield();
			}
			else if (msg.getTitle().equals(Message.QUEUE_START)) {
				this.progressBar.setIndeterminate(true);
				this.progressBar.startAnimation(null);
			}
			else if (msg.getTitle().equals(Message.QUEUE_STOP)) {
				this.progressBar.setIndeterminate(false);
				this.progressBar.stopAnimation(null);
			}
		}
	}
	
	private void updateProgressbar() {
		double progressValue = 0;
		if (queue.getSize() > 0) {
			progressValue = queue.getCurrent()/queue.getSize();
			this.progressBar.setIndeterminate(false);
			this.progressBar.setMinValue(0);
			this.progressBar.setMaxValue((double)queue.getSize());
			this.progressBar.setDoubleValue((double)queue.getCurrent());
		}
		else {
			this.progressBar.setIndeterminate(true);
		}
	}
	
	private void updateProgressfield() {
		//@todo change color when highlighted
		this.filenameField.setAttributedStringValue(new NSAttributedString(queue.getRoot().getName(),
																		   TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
		this.progressField.setAttributedStringValue(new NSAttributedString(queue.getStatusText(),
																		   TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
	}

	public Queue getQueue() {
		return this.queue;
	}
	
	private NSTextField filenameField;
	
	public void setFilenameField(NSTextField filenameField) {
		this.filenameField = filenameField;
	}

	private NSTextField progressField;
	
	public void setProgressField(NSTextField progressField) {
		this.progressField = progressField;
	}
		
	private NSProgressIndicator progressBar;
	
	public void setProgressBar(NSProgressIndicator progressBar) {
		this.progressBar = progressBar;
		this.progressBar.setIndeterminate(false);
		this.progressBar.setDisplayedWhenStopped(true);
		this.progressBar.setControlTint(NSProgressIndicator.BlueControlTint);
		this.progressBar.setControlSize(NSProgressIndicator.SmallControlSize);
		this.progressBar.setStyle(NSProgressIndicator.ProgressIndicatorBarStyle);
	}
	
	private NSView progressView;
	
	public void setProgressSubview(NSView progressView) {
		this.progressView = progressView;
	}
	
	public NSView view() {
		return this.progressView;
	}
	
	public void awakeFromNib() {
		log.debug("awakeFromNib");
		this.updateProgressfield();
		this.updateProgressbar();
	}
}

