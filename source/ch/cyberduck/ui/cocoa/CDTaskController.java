package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.NSButton;
import com.apple.cocoa.application.NSProgressIndicator;
import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSSelector;

import ch.cyberduck.ui.cocoa.threading.BackgroundAction;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionListener;
import ch.cyberduck.ui.cocoa.threading.DefaultMainAction;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class CDTaskController extends CDBundleController {
    private static Logger log = Logger.getLogger(CDTaskController.class);

    private NSTextField name;

    public void setName(NSTextField name) {
        this.name = name;
        this.name.setStringValue(task.toString());
    }

    private NSTextField text;

    public void setText(NSTextField text) {
        this.text = text;
        this.text.setStringValue(task.getActivity());
    }

    private NSProgressIndicator progress;

    public void setProgress(NSProgressIndicator progress) {
        this.progress = progress;
        this.progress.setDisplayedWhenStopped(false);
        this.progress.setIndeterminate(true);
    }

    private NSButton stopButton;

    public void setStopButton(NSButton stopButton) {
        this.stopButton = stopButton;
        this.stopButton.setTarget(this);
        this.stopButton.setAction(new NSSelector("stopButtonClicked", new Class[]{NSButton.class}));
    }

    public void stopButtonClicked(Object sender) {
        task.cancel();
    }

    private NSView view;

    public void setView(NSView view) {
        this.view = view;
    }

    public NSView view() {
        return view;
    }

    private BackgroundAction task;

    public CDTaskController(final BackgroundAction task) {
        this.task = task;
        this.task.addListener(new BackgroundActionListener() {
            public void start(BackgroundAction action) {
                CDMainApplication.invoke(new DefaultMainAction() {
                    public void run() {
                        progress.startAnimation(null);
                    }
                });
            }

            public void stop(BackgroundAction action) {
                CDMainApplication.invoke(new DefaultMainAction() {
                    public void run() {
                        progress.stopAnimation(null);
                    }
                });
            }
        });
        this.loadBundle();
    }

    public void awakeFromNib() {
        ;
    }

    protected String getBundleName() {
        return "Task";
    }
}