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

import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionListener;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.ui.cocoa.application.NSButton;
import ch.cyberduck.ui.cocoa.application.NSProgressIndicator;
import ch.cyberduck.ui.cocoa.application.NSTextField;
import ch.cyberduck.ui.cocoa.application.NSView;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;

/**
 * @version $Id$
 */
public class TaskController extends BundleController {
    private static Logger log = Logger.getLogger(TaskController.class);

    @Outlet
    private NSTextField name;

    public void setName(NSTextField name) {
        this.name = name;
        this.name.setStringValue(task.toString());
    }

    @Outlet
    private NSTextField text;

    public void setText(NSTextField text) {
        this.text = text;
        this.text.setStringValue(task.getActivity());
    }

    @Outlet
    private NSProgressIndicator progress;

    public void setProgress(NSProgressIndicator progress) {
        this.progress = progress;
        this.progress.setDisplayedWhenStopped(false);
        this.progress.setIndeterminate(true);
    }

    @Outlet
    private NSButton stopButton;

    public void setStopButton(NSButton stopButton) {
        this.stopButton = stopButton;
        this.stopButton.setTarget(this.id());
        this.stopButton.setAction(Foundation.selector("stopButtonClicked:"));
    }

    public void stopButtonClicked(final ID sender) {
        task.cancel();
    }

    @Outlet
    private NSView view;

    public void setView(NSView view) {
        this.view = view;
    }

    @Override
    public NSView view() {
        return view;
    }

    private BackgroundAction task;

    public TaskController(final BackgroundAction task) {
        this.task = task;
        this.loadBundle();
        if(this.task.isRunning()) {
            progress.startAnimation(null);
        }
        this.task.addListener(new BackgroundActionListener() {
            public void start(BackgroundAction action) {
                invoke(new DefaultMainAction() {
                    public void run() {
                        progress.startAnimation(null);
                    }
                });
            }

            public void cancel(BackgroundAction action) {
                invoke(new DefaultMainAction() {
                    public void run() {
                        progress.stopAnimation(null);
                    }
                });
            }

            public void stop(BackgroundAction action) {
                invoke(new DefaultMainAction() {
                    public void run() {
                        progress.stopAnimation(null);
                    }
                });
                action.removeListener(this);
            }
        });
    }

    @Override
    protected String getBundleName() {
        return "Task";
    }
}