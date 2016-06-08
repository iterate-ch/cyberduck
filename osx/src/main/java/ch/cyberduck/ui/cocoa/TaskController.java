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

import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSProgressIndicator;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionListener;

import org.rococoa.Foundation;
import org.rococoa.ID;

public class TaskController extends BundleController {

    private BackgroundAction task;

    @Outlet
    private NSTextField name;

    public void setName(NSTextField name) {
        this.name = name;
        this.name.setStringValue(task.getName());
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

    public TaskController(final BackgroundAction task) {
        this.task = task;
        this.loadBundle();
        if(this.task.isRunning()) {
            progress.startAnimation(null);
        }
        this.task.addListener(new BackgroundActionListener() {
            public void start(BackgroundAction action) {
                progress.startAnimation(null);
            }

            public void cancel(BackgroundAction action) {
                progress.stopAnimation(null);
            }

            public void stop(BackgroundAction action) {
                progress.stopAnimation(null);
                action.removeListener(this);
            }

            @Override
            public boolean alert(final Host host, final BackgroundException failure,
                                 final StringBuilder transcript) {
                return false;
            }
        });
    }

    @Override
    protected String getBundleName() {
        return "Task";
    }
}