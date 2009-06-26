package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.threading.BackgroundAction;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @version $Id$
 */
public abstract class CDWindowController extends CDBundleController {
    private static Logger log = Logger.getLogger(CDWindowController.class);

    protected static final String DEFAULT = NSBundle.localizedString("Default", "");

    public CDWindowController() {
        super();
    }

    /**
     * Called by the runtime after the NIB file has been loaded sucessfully
     */
    public abstract void awakeFromNib();

    /**
     * Will queue up the <code>BackgroundAction</code> to be run in a background thread. Will be executed
     * as soon as no other previous <code>BackgroundAction</code> is pending.
     *
     * @param runnable The runnable to execute in a secondary Thread
     * @return Will return immediatly but not run the runnable before the lock of the runnable is acquired.
     * @see java.lang.Thread
     * @see ch.cyberduck.ui.cocoa.threading.BackgroundAction#lock()
     */
    public void background(final BackgroundAction runnable) {
        runnable.init();
        // Start background task
        new Thread("Background") {
            public void run() {
                // Synchronize all background threads to this lock so actions run
                // sequentially as they were initiated from the main interface thread
                synchronized(runnable.lock()) {
                    log.info("Acquired lock for background runnable:" + runnable);
                    // An autorelease pool is used to manage Foundation's autorelease
                    // mechanism for Objective-C objects. If you start off a thread
                    // that calls Cocoa, there won't be a top-level pool.
                    final int pool = NSAutoreleasePool.push();
                    try {
                        if(runnable.prepare()) {
                            // Execute the action of the runnable
                            runnable.run();
                        }
                    }
                    finally {
                        // Increase the run counter
                        runnable.finish();
                        // Invoke the cleanup on the main thread to let the action
                        // synchronize the user interface
                        CDMainApplication.invoke(new WindowMainAction(CDWindowController.this) {
                            public void run() {
                                runnable.cleanup();
                            }
                        });

                        // Indicates that you are finished using the
                        // NSAutoreleasePool identified by pool.
                        NSAutoreleasePool.pop(pool);

                        log.info("Releasing lock for background runnable:" + runnable);
                    }
                }
            }
        }.start();
        log.info("Started background runnable:" + runnable);
    }

    /**
     * The window this controller is owner of
     */
    protected NSWindow window; // IBOutlet

    private Set<CDWindowListener> listeners
            = Collections.synchronizedSet(new HashSet<CDWindowListener>());

    /**
     * @param listener
     */
    public void addListener(CDWindowListener listener) {
        listeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeListener(CDWindowListener listener) {
        listeners.remove(listener);
    }

    public void setWindow(NSWindow window) {
        this.window = window;
        NSNotificationCenter.defaultCenter().addObserver(this,
                new NSSelector("windowWillClose", new Class[]{NSNotification.class}),
                NSWindow.WindowWillCloseNotification,
                this.window);
        this.window.setReleasedWhenClosed(true);
    }

    public NSWindow window() {
        return this.window;
    }

    /**
     * @see com.apple.cocoa.application.NSWindow.Delegate
     */
    public boolean windowShouldClose(NSWindow sender) {
        return true;
    }

    /**
     * Override this method if the controller should not be invalidated after its window closes
     *
     * @param notification
     */
    public void windowWillClose(NSNotification notification) {
        log.debug("windowWillClose:" + notification);
        for(CDWindowListener listener : listeners) {
            listener.windowWillClose();
        }
        //If the window is closed it is assumed the controller object is no longer used
        this.invalidate();
    }

    protected void invalidate() {
        this.window = null;
        this.listeners.clear();
        super.invalidate();
    }

    /**
     * @return False if the window has been released
     */
    public boolean isShown() {
        return this.window != null;
    }

    /**
     * Position this controller's window relative to other open windows
     */
    public void cascade() {
        NSArray windows = NSApplication.sharedApplication().windows();
        int count = windows.count();
        if(count != 0) {
            NSWindow window = (NSWindow) windows.objectAtIndex(count - 1);
            NSPoint origin = window.frame().origin();
            origin = new NSPoint(origin.x(), origin.y() + window.frame().size().height());
            this.window.setFrameTopLeftPoint(this.window.cascadeTopLeftFromPoint(origin));
        }
    }

    /**
     * @param toggle
     * @param open
     */
    protected void setState(NSButton toggle, boolean open) {
        if(open) {
            toggle.performClick(null);
        }
        toggle.setState(open ? NSCell.OnState : NSCell.OffState);
    }

    /**
     * @return True if this window has a sheet attached
     */
    public boolean hasSheet() {
        if(null == this.window) {
            return false;
        }
        return this.window.attachedSheet() != null;
    }

    /**
     * Attach a sheet to this window
     *
     * @param sheet The sheet to be attached to this window
     * @see ch.cyberduck.ui.cocoa.CDSheetController#beginSheet()
     */
    protected void alert(final NSWindow sheet) {
        this.alert(sheet, new CDSheetCallback() {
            public void callback(final int returncode) {
                ;
            }
        });
    }

    /**
     * Attach a sheet to this window
     *
     * @param sheet    The sheet to be attached to this window
     * @param callback The callback to call after the sheet is dismissed
     * @see ch.cyberduck.ui.cocoa.CDSheetController#beginSheet()
     */
    protected void alert(final NSWindow sheet, final CDSheetCallback callback) {
        CDSheetController c = new CDSheetController(this, sheet) {
            public void callback(final int returncode) {
                callback.callback(returncode);
            }
        };
        c.beginSheet();
    }


    protected void updateField(final NSTextView f, final String value) {
        f.setString(StringUtils.isNotBlank(value) ? value : "");
    }

    protected void updateField(final NSTextField f, final String value) {
        f.setStringValue(StringUtils.isNotBlank(value) ? value : "");
    }

    public void helpButtonClicked(final NSButton sender) {
        try {
            NSWorkspace.sharedWorkspace().openURL(
                    new java.net.URL(Preferences.instance().getProperty("website.help"))
            );
        }
        catch(java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
    }
}