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

import java.util.Iterator;
import java.util.Observable;
import java.util.List;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * @version $Id$
 */
public abstract class CDValidatorController extends AbstractValidator {
	protected static Logger log = Logger.getLogger(CDValidatorController.class);

	private static NSMutableArray instances = new NSMutableArray();

	public CDValidatorController(boolean resumeRequested) {
		super(resumeRequested);
		instances.addObject(this);
	}

	public void awakeFromNib() {
		this.fileTableView.setDataSource(this);
		this.fileTableView.sizeToFit();
		(NSNotificationCenter.defaultCenter()).addObserver(this,
		    new NSSelector("tableViewSelectionDidChange", new Class[]{NSNotification.class}),
		    NSTableView.TableViewSelectionDidChangeNotification,
		    this.fileTableView);
	}

	public synchronized void update(Observable observable, Object arg) {
		if(arg instanceof Message) {
			Message msg = (Message)arg;
			if(msg.getTitle().equals(Message.ERROR)) {
				if(CDQueueController.instance().hasSheet()) {
					NSApplication.sharedApplication().endSheet(this.window());//@todo send return code
				}
			}
		}
	}
	
	public List validate(Queue q) {
		q.addObserver(this);
		synchronized(CDQueueController.instance()) {
			for(Iterator iter = q.getChilds().iterator(); iter.hasNext() && !this.isCanceled(); ) {
				Path child = (Path)iter.next();
				if(this.validate(child)) {
					this.validated.add(child);
				}
				if(this.visible) {
					this.fireDataChanged();
				}
			}
			if(this.visible && !this.isCanceled()) {
				this.statusIndicator.stopAnimation(null);
				this.setEnabled(true);
			}
		}
		q.deleteObserver(this);
		return this.getResult();
	}

	protected abstract List getResult();

	protected abstract void load();
	
	protected boolean visible = false;
	
	protected void prompt(Path p) {
		log.debug("prompt:"+p);
		if(!this.visible) {
			this.load();
			CDQueueController.instance().beginSheet(this.window());
			this.statusIndicator.startAnimation(null);
			this.visible = true;
		}
		this.workset.add(p);
	}
	
	protected void adjustFilename(Path path) {
		//
	}
	
	// ----------------------------------------------------------
	// Outlets
	// ----------------------------------------------------------
	
	private NSTextField infoLabel; // IBOutlet

	public void setInfoLabel(NSTextField infoLabel) {
		this.infoLabel = infoLabel;
	}

	private NSTextField urlField; // IBOutlet

	public void setUrlField(NSTextField urlField) {
		this.urlField = urlField;
	}

	private NSTextField localField; // IBOutlet

	public void setLocalField(NSTextField localField) {
		this.localField = localField;
	}

	private NSProgressIndicator statusIndicator; // IBOutlet

	public void setStatusIndicator(NSProgressIndicator statusIndicator) {
		this.statusIndicator = statusIndicator;
	}

	private NSTableView fileTableView; // IBOutlet

	public void setFileTableView(NSTableView fileTableView) {
		this.fileTableView = fileTableView;
	}

	private NSButton skipButton; // IBOutlet

	public void setSkipButton(NSButton skipButton) {
		this.skipButton = skipButton;
		this.skipButton.setEnabled(false);
	}

	private NSButton resumeButton; // IBOutlet

	public void setResumeButton(NSButton resumeButton) {
		this.resumeButton = resumeButton;
		this.resumeButton.setEnabled(false);
		//@todo resumeButton.setEnabled(path.status.getCurrent() < path.status.getSize());
	}

	private NSButton overwriteButton; // IBOutlet

	public void setOverwriteButton(NSButton overwriteButton) {
		this.overwriteButton = overwriteButton;
		this.overwriteButton.setEnabled(false);
	}

	private NSPanel window; // IBOutlet

	public void setWindow(NSPanel window) {
		this.window = window;
		this.window.setDelegate(this);
	}

	public NSPanel window() {
		return this.window;
	}

	protected void setEnabled(boolean enabled) {
		this.overwriteButton.setEnabled(enabled);
		this.resumeButton.setEnabled(enabled); //@todo
		this.skipButton.setEnabled(enabled);
	}

	protected void reloadTable() {
		this.fileTableView.reloadData();
		this.infoLabel.setStringValue(this.workset.size()+" "+NSBundle.localizedString("files", ""));
	}

	public void windowWillClose(NSNotification notification) {
		instances.removeObject(this);
	}

	protected void fireDataChanged() {
		this.reloadTable();
	}

	public void resumeActionFired(NSButton sender) {
		log.debug("resumeActionFired");
		for(Iterator i = this.workset.iterator(); i.hasNext();) {
			((Path)i.next()).status.setResume(true);
		}
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	public void overwriteActionFired(NSButton sender) {
		log.debug("overwriteActionFired");
		for(Iterator i = this.workset.iterator(); i.hasNext();) {
			((Path)i.next()).status.setResume(false);
		}
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	public void skipActionFired(NSButton sender) {
		log.debug("skipActionFired");
		this.workset.clear();
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	public void cancelActionFired(NSButton sender) {
		log.debug("cancelActionFired");
		this.setCanceled(true);
		this.validated.clear();
		this.workset.clear();
		NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
	}

	private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();

	static {
		lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
	}

	private static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingMiddleParagraph},
	    new Object[]{NSAttributedString.ParagraphStyleAttributeName});
	
	// ----------------------------------------------------------
	// NSTableView.DataSource
	// ----------------------------------------------------------
	
	public void tableViewSelectionDidChange(NSNotification notification) {
		if(this.fileTableView.selectedRow() != -1) {
			Path p = (Path)this.workset.get(this.fileTableView.selectedRow());
			if(p != null) {
				if(p.getLocal().exists()) {
					this.localField.setAttributedStringValue(new NSAttributedString(p.getLocal().getAbsolute(),
					    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
				}
				else {
					this.localField.setStringValue("-");
				}
				if(p.getRemote().exists(false)) {
					this.urlField.setAttributedStringValue(new NSAttributedString(p.getRemote().getHost().getURL()+p.getRemote().getAbsolute(),
					    TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
				}
			}
		}
		else {
			this.urlField.setStringValue("-");
			this.localField.setStringValue("-");
		}
	}

	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
		if(row < this.numberOfRowsInTableView(tableView)) {
			String identifier = (String)tableColumn.identifier();
			Path p = (Path)this.workset.get(row);
			if(p != null) {
				if(identifier.equals("FILENAME")) {
					return new NSAttributedString(p.getRemote().getName());
				}
				if(identifier.equals("ICON")) {
					NSImage icon = CDIconCache.instance().get(p.getExtension());
					icon.setSize(new NSSize(16f, 16f));
					return icon;
				}
				if(identifier.equals("TOOLTIP")) {
					StringBuffer tooltip = new StringBuffer();
					if(p.exists(false)) {
						tooltip.append(NSBundle.localizedString("Remote", "")+":\n"
						    +"  "+Status.getSizeAsString(p.status.getSize())+"\n"
						    +"  "+p.attributes.getTimestampAsString());
					}
					if(p.exists(false) && p.getLocal().exists())
						tooltip.append("\n");
					if(p.getLocal().exists()) {
						tooltip.append(NSBundle.localizedString("Local", "")+":\n"
						    +"  "+Status.getSizeAsString(p.getLocal().length())+"\n"
						    +"  "+p.getLocal().getTimestampAsString());
					}
					return tooltip.toString();
				}
			}
		}
		return null;
	}

	public int numberOfRowsInTableView(NSTableView tableView) {
		return this.workset.size();
	}
}