package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
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
import ch.cyberduck.core.threading.DefaultMainAction;

import org.rococoa.Foundation;
import org.rococoa.ID;

public class TaskController extends BundleController {

    private final BackgroundAction task;

    @Outlet
    private NSTextField name;
    @Outlet
    private NSTextField text;
    @Outlet
    private NSProgressIndicator progress;
    @Outlet
    private NSButton stopButton;
    @Outlet
    private NSView view;

    public TaskController(final BackgroundAction task) {
        this.task = task;
        this.loadBundle();
        if(this.task.isRunning()) {
            progress.startAnimation(null);
        }
        this.task.addListener(new BackgroundActionListener() {
            public void start(BackgroundAction action) {
                invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        progress.startAnimation(null);
                    }
                });
            }

            public void cancel(BackgroundAction action) {
                invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        progress.stopAnimation(null);
                    }
                });
            }

            public void stop(BackgroundAction action) {
                invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        progress.stopAnimation(null);
                    }
                });
                action.removeListener(this);
            }

            @Override
            public boolean alert(final Host host, final BackgroundException failure,
                                 final StringBuilder transcript) {
                return false;
            }
        });
    }

    public void setName(NSTextField name) {
        this.name = name;
        this.name.setStringValue(task.getName());
    }

    public void setText(NSTextField text) {
        this.text = text;
        this.text.setStringValue(task.getActivity());
    }

    public void setProgress(NSProgressIndicator progress) {
        this.progress = progress;
        this.progress.setDisplayedWhenStopped(false);
        this.progress.setIndeterminate(true);
    }

    public void setStopButton(NSButton stopButton) {
        this.stopButton = stopButton;
        this.stopButton.setTarget(this.id());
        this.stopButton.setAction(Foundation.selector("stopButtonClicked:"));
    }

    public void stopButtonClicked(final ID sender) {
        task.cancel();
    }

    public void setView(NSView view) {
        this.view = view;
    }

    @Override
    public NSView view() {
        return view;
    }

    @Override
    protected String getBundleName() {
        return "Task";
    }
}