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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Status;
import ch.cyberduck.core.AbstractValidator;

/**
 * @version $Id$
 */
public abstract class CDValidatorController extends AbstractValidator {
    protected static Logger log = Logger.getLogger(CDValidatorController.class);

    private static NSMutableArray instances = new NSMutableArray();

    protected CDController windowController = CDQueueController.instance();

    public CDValidatorController(boolean resumeRequested) {
		super(resumeRequested);
//        this.windowController = windowController; //@todo CDQueueController.instance()
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
//		this.fileTableView.deselectAll(null);
		this.fileTableView.reloadData();
		this.infoLabel.setStringValue(this.workset.size()+" "+NSBundle.localizedString("files", ""));
	}
		
    public void windowWillClose(NSNotification notification) {
        instances.removeObject(this);
    }

	protected void fireDataChanged() {
		this.reloadTable();
	}
	
	protected abstract void load();

	protected synchronized void prompt() {
		this.load();
        while (this.windowController.window().attachedSheet() != null) {
            try {
                log.debug("Sleeping..."); this.wait();
            }
            catch (InterruptedException e) {
                log.error(e.getMessage());
				return;
            }
        }
		this.windowController.window().makeKeyAndOrderFront(null);
		NSApplication.sharedApplication().beginSheet(this.window(), //sheet
													 this.windowController.window(),
													 this, //modalDelegate
													 new NSSelector("validateSheetDidEnd",
																	new Class[]{NSWindow.class, int.class, Object.class}), // did end selector
													 null); //contextInfo
		this.windowController.window().makeKeyAndOrderFront(null);
		this.statusIndicator.startAnimation(null);
	}
	
	/* 
		@todo !!!
	 List list = path.getParent().childs();
	 if (list.indexOf(path) != -1) {
		 remote = (Path)list.get(list.indexOf(path));
	 }
	 */
	
	protected void proposeFilename(Path path) {
//		return path.local.getName();
	}

    public void resumeActionFired(NSButton sender) {
        log.debug("resumeActionFired");
		for(Iterator i = this.workset.iterator(); i.hasNext(); ) {
			((Path)i.next()).status.setResume(true);
		}
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }

    public void overwriteActionFired(NSButton sender) {
        log.debug("overwriteActionFired");
		for(Iterator i = this.workset.iterator(); i.hasNext(); ) {
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
        this.setCanceled(true);
		this.workset.clear();
        NSApplication.sharedApplication().endSheet(this.window(), sender.tag());
    }

	public synchronized boolean validate(Queue q) {
		boolean visible = false;
		// for every root get its childs
		for (Iterator rootIter = q.getRoots().iterator(); rootIter.hasNext(); ) {
			for(Iterator iter = q.getChilds((Path)rootIter.next()).iterator(); iter.hasNext(); ) {
				Path child = (Path)iter.next();
				if (this.validate(child)) {
					log.info(child.getName()+" validated.");
					this.validated.add(child);
				}
				else {
					log.info(child.getName()+" in workset.");
					if(!visible) {
						this.prompt(); visible = true;
					}
					this.workset.add(child);
				}
				if(visible)
					this.fireDataChanged();
			}
		}
		if(loaded) {
			this.statusIndicator.stopAnimation(null);
			this.setEnabled(true);

			while (this.windowController.window().attachedSheet() != null) {
				try {
					log.debug("Sleeping..."); this.wait();
				}
				catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			}
		}
		return !this.isCanceled();
	}
	
	public synchronized void validateSheetDidEnd(NSWindow sheet, int returncode, Object contextInfo) {
        sheet.close();
		this.notify();
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
																					TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY)
															 );
				}
				else {
					this.localField.setStringValue("-");
				}
				if(p.getRemote().exists()) {
					this.urlField.setAttributedStringValue(new NSAttributedString(p.getRemote().getHost().getURL()+p.getRemote().getAbsolute(), 
																				  TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY)
														   );
				}
			}
		}
		else {
			this.urlField.setStringValue("-");
			this.localField.setStringValue("-");
		}
	}
	
	public Object tableViewObjectValueForLocation(NSTableView tableView, NSTableColumn tableColumn, int row) {
        if (row < numberOfRowsInTableView(tableView)) {
            String identifier = (String)tableColumn.identifier();
			Path p = (Path)this.workset.get(row);
			if(p != null) {
				if (identifier.equals("NAME")) {
					return p.getRemote().getName();
				}
				if (identifier.equals("ICON")) {
					NSImage icon = CDIconCache.instance().get(p.getExtension());
					icon.setSize(new NSSize(16f, 16f));
					return icon;
				}
				if (identifier.equals("TOOLTIP")) {
					StringBuffer tooltip = new StringBuffer();
					if(p.exists())
						tooltip.append(NSBundle.localizedString("Remote", "")+":\n"
									   +"  "+Status.getSizeAsString(p.status.getSize())+"\n"
									   +"  "+p.attributes.getTimestampAsString()+"\n");
					if(p.getLocal().exists())
						tooltip.append(NSBundle.localizedString("Local", "")+":\n"
									   +"  "+Status.getSizeAsString(p.getLocal().length())+"\n"
									   +"  "+p.getLocal().getTimestampAsString()+"\n");
					return tooltip.toString();
				}
			}
		}
		return null;
	}
	
	public int numberOfRowsInTableView(NSTableView tableView) {
		return workset.size();
    }
}