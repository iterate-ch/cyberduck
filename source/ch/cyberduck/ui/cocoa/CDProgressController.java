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

import java.util.Observable;
import java.util.Observer;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.ui.cocoa.growl.Growl;
import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public class CDProgressController extends NSObject implements Observer {
	private static Logger log = Logger.getLogger(CDProgressController.class);

	private String statusText = "";
	private StringBuffer errorText;
//	private StringBuffer tooltip;

	private NSTimer progressTimer;
	
	private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();
	private static NSMutableParagraphStyle lineBreakByTruncatingTailParagraph = new NSMutableParagraphStyle();

	static {
		lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
		lineBreakByTruncatingTailParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingTail);
	}

	private static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingMiddleParagraph},
	    new Object[]{NSAttributedString.ParagraphStyleAttributeName});
	private static final NSDictionary TRUNCATE_TAIL_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingTailParagraph},
	    new Object[]{NSAttributedString.ParagraphStyleAttributeName});


	private Queue queue;

	public CDProgressController(Queue queue) {
		this.queue = queue;
		this.queue.addObserver(this);
		this.queue.getRoot().getSession().addObserver(this);
		//@todo this.queue.deleteObserver(this);
		//@todo this.queue.getRoot().getSession().deleteObserver(this);
		if(false == NSApplication.loadNibNamed("Progress", this)) {
			log.fatal("Couldn't load Progress.nib");
		}
	}
	
	public void awakeFromNib() {
		log.debug("awakeFromNib");
		this.filenameField.setAttributedStringValue(new NSAttributedString(this.queue.getName(),
																		   TRUNCATE_TAIL_PARAGRAPH_DICTIONARY));
		this.updateProgressfield();
	}
	
	public void update(NSTimer t) {
		this.updateProgressbar();
		this.updateProgressfield();
	}
	
	public void update(final Observable o, final Object arg) {
		if(arg instanceof Message) {
//			ThreadUtilities.instance().invokeLater(new Runnable() {
//				public void run() {
					Message msg = (Message)arg;
					if(msg.getTitle().equals(Message.PROGRESS)) {
						statusText = (String)msg.getContent();
						updateProgressfield();
					}
					else if(msg.getTitle().equals(Message.ERROR)) {
						errorText.append("\n"+(String)msg.getContent());
						alertIcon.setHidden(false);
					}
					else if(msg.getTitle().equals(Message.QUEUE_START)) {
						progressBar.setIndeterminate(true);
						progressBar.startAnimation(null);
						errorText = new StringBuffer();
						alertIcon.setHidden(true);
						progressTimer = new NSTimer(0.5, //seconds
													CDProgressController.this, //target
													new NSSelector("update", new Class[]{NSTimer.class}),
													getQueue(), //userInfo
													true); //repeating
						NSRunLoop.currentRunLoop().addTimerForMode(progressTimer, NSRunLoop.DefaultRunLoopMode);
					}
					else if(msg.getTitle().equals(Message.QUEUE_STOP)) {
						updateProgressbar();
						updateProgressfield();
						progressBar.stopAnimation(null);
						progressBar.setIndeterminate(false);
						if(queue.isComplete() && !queue.isCanceled()) {
							if(queue instanceof DownloadQueue) {
								Growl.instance().notify(NSBundle.localizedString("Download complete",
																				 "Growl Notification"),
														queue.getName());
								if(Preferences.instance().getProperty("queue.postProcessItemWhenComplete").equals("true")) {
									boolean success = NSWorkspace.sharedWorkspace().openFile(queue.getRoot().getLocal().toString());
									log.debug("Success opening file:"+success);
								}
							}
							if(queue instanceof UploadQueue) {
								Growl.instance().notify(NSBundle.localizedString("Upload complete",
																				 "Growl Notification"),
														queue.getName());
							}
							if(queue instanceof SyncQueue) {
								Growl.instance().notify(NSBundle.localizedString("Synchronization complete",
																				 "Growl Notification"),
														queue.getName());
							}
							if(Preferences.instance().getProperty("queue.removeItemWhenComplete").equals("true")) {
								CDQueueController.instance().removeItem(queue);
							}
						}
						progressTimer.invalidate();
					}
//				}
//			});
		}
	}
	
	private void updateProgressbar() {
		if(queue.isInitialized()) {
			double progressValue = queue.getCurrent()/queue.getSize();
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
		this.progressField.setAttributedStringValue(new NSAttributedString(this.getProgressText(),
		    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
	}
	
	private String getProgressText() {
		if(queue.isInitialized()) {
			if(this.queue.isRunning()) {
				return this.queue.getCurrentAsString()
				+" "+NSBundle.localizedString("of", "1.2MB of 3.4MB")+" "+this.queue.getSizeAsString()
				+" "+this.queue.getSpeedAsString()+" "+this.statusText;
			}
			return this.queue.getCurrentAsString()
			+" "+NSBundle.localizedString("of", "1.2MB of 3.4MB")+" "+this.queue.getSizeAsString()+"  "+this.statusText;
		}
		return "("+NSBundle.localizedString("Unknown size", "")+")  "+this.statusText;
	}
	
	private String getErrorText() {
		return this.errorText.toString();
	}
	
	public Queue getQueue() {
		return this.queue;
	}

	public void alertButtonClicked(NSButton sender) {
		CDQueueController.instance().beginSheet(NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Error", "Alert sheet title"),
																				this.getErrorText(), // message
																				NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
																				null, //alternative button
																				null //other button
																				)
												);
	}
		
	private boolean highlighted;

	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
		if(highlighted) {
			this.filenameField.setTextColor(NSColor.whiteColor());
			this.progressField.setTextColor(NSColor.whiteColor());
		}
		else {
			this.filenameField.setTextColor(NSColor.blackColor());
			this.progressField.setTextColor(NSColor.darkGrayColor());
		}
	}

	public boolean isHighlighted() {
		return this.highlighted;
	}
	
	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------
	
	private NSTextField filenameField; // IBOutlet

	public void setFilenameField(NSTextField filenameField) {
		this.filenameField = filenameField;
		this.filenameField.setEditable(false);
		this.filenameField.setSelectable(false);
		this.filenameField.setTextColor(NSColor.blackColor());
	}

	private NSTextField progressField; // IBOutlet

	public void setProgressField(NSTextField progressField) {
		this.progressField = progressField;
		this.progressField.setEditable(false);
		this.progressField.setSelectable(false);
		this.progressField.setTextColor(NSColor.darkGrayColor());
	}

	private NSProgressIndicator progressBar; // IBOutlet

	public void setProgressBar(NSProgressIndicator progressBar) {
		this.progressBar = progressBar;
		this.progressBar.setIndeterminate(false);
		this.progressBar.setDisplayedWhenStopped(false);
		this.progressBar.setControlTint(NSProgressIndicator.GraphiteControlTint);
		this.progressBar.setControlTint(NSProgressIndicator.BlueControlTint);
		this.progressBar.setControlSize(NSProgressIndicator.SmallControlSize);
		this.progressBar.setStyle(NSProgressIndicator.ProgressIndicatorBarStyle);
		this.progressBar.setUsesThreadedAnimation(true);
	}

	private NSButton alertIcon; // IBOutlet

	public void setAlertIcon(NSButton alertIcon) {
		this.alertIcon = alertIcon;
		this.alertIcon.setHidden(true);
		this.alertIcon.setTarget(this);
		this.alertIcon.setAction(new NSSelector("alertButtonClicked", new Class[]{Object.class}));
	}

	private NSView progressView; // IBOutlet

	public void setProgressSubview(NSView progressView) {
		this.progressView = progressView;
	}

	public NSView view() {
		return this.progressView;
	}
}