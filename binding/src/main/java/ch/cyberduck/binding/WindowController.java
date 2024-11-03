package ch.cyberduck.binding;

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

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSPrintInfo;
import ch.cyberduck.binding.application.NSPrintOperation;
import ch.cyberduck.binding.application.NSPrintPanel;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.application.WindowListener;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.local.BrowserLauncherFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class WindowController extends BundleController implements NSWindow.Delegate {
    private static final Logger log = LogManager.getLogger(WindowController.class);

    protected static final String DEFAULT = LocaleFactory.localizedString("Default");

    private final Set<WindowListener> listeners
            = Collections.synchronizedSet(new HashSet<WindowListener>());
    /**
     * The window this controller is owner of
     */
    @Outlet
    protected NSWindow window;

    public WindowController() {
        super();
    }

    @Override
    public void invalidate() {
        listeners.clear();
        if(window != null) {
            window.setDelegate(null);
        }
        super.invalidate();
    }

    /**
     * @param listener Callback on window close
     */
    public void addListener(final WindowListener listener) {
        listeners.add(listener);
    }

    /**
     * @param listener Callback on window close
     */
    public void removeListener(final WindowListener listener) {
        listeners.remove(listener);
    }

    public void setWindow(final NSWindow window) {
        this.window = window;
        this.window.recalculateKeyViewLoop();
        this.window.setReleasedWhenClosed(!this.isSingleton());
        this.window.setDelegate(this.id());
    }

    public NSWindow window() {
        return window;
    }

    /**
     * A singleton window is not released when closed and the controller is not invalidated
     *
     * @return Always false
     * @see #invalidate()
     * @see ch.cyberduck.binding.application.NSWindow#setReleasedWhenClosed(boolean)
     */
    public boolean isSingleton() {
        return false;
    }

    /**
     * @return True if the controller window is on screen.
     */
    public boolean isVisible() {
        if(null == window) {
            return false;
        }
        return window.isVisible();
    }

    @Override
    @Delegate
    public void windowDidBecomeKey(final NSNotification notification) {
        log.debug("Become key for window {}", window);
    }

    @Override
    @Delegate
    public void windowDidResignKey(final NSNotification notification) {
        log.debug("Resign key for window {}", window);
    }

    @Override
    @Delegate
    public void windowDidBecomeMain(final NSNotification notification) {
        log.debug("Become main for window {}", window);
    }

    @Override
    @Delegate
    public void windowDidResignMain(final NSNotification notification) {
        log.debug("Resign main for window {}", window);
    }

    @Delegate
    public void windowWillEnterFullScreen(final NSNotification notification) {
        log.debug("Enter full screen for window {}", window);
    }

    @Delegate
    public void windowWillExitFullScreen(final NSNotification notification) {
        log.debug("Exit full screen for window {}", window);
    }

    @Delegate
    public void windowDidFailToEnterFullScreen(final NSWindow window) {
        log.error("Error entering full screen");
    }

    @Override
    @Delegate
    public void windowWillBeginSheet(final NSNotification notification) {
        log.debug("Attach sheet for window {}", window);
    }

    /**
     * @see ch.cyberduck.binding.application.NSWindow.Delegate
     */
    @Override
    @Delegate
    public boolean windowShouldClose(final NSWindow sender) {
        return true;
    }

    /**
     * Override this method if the controller should not be invalidated after its window closes
     */
    @Override
    @Delegate
    public void windowWillClose(final NSNotification notification) {
        window.endEditingFor(null);
        log.debug("Window will close {}", notification);
        for(WindowListener listener : listeners.toArray(new WindowListener[listeners.size()])) {
            listener.windowWillClose();
        }
        if(!this.isSingleton()) {
            //If the window is closed it is assumed the controller object is no longer used
            this.invalidate();
        }
    }

    /**
     * Position this controller's window relative to other open windows
     */
    protected NSPoint cascade(final NSPoint point) {
        return window.cascadeTopLeftFromPoint(point);
    }

    /**
     * Resize window frame to fit the content view of the currently selected tab.
     */
    protected void resize() {
        final NSRect windowFrame = NSWindow.contentRectForFrameRect_styleMask(window.frame(), window.styleMask());
        final double height = this.getMinWindowHeight();
        final NSRect frameRect = new NSRect(
                new NSPoint(windowFrame.origin.x.doubleValue(), windowFrame.origin.y.doubleValue() + windowFrame.size.height.doubleValue() - height),
                new NSSize(windowFrame.size.width.doubleValue(), height)
        );
        window.setFrame_display_animate(NSWindow.frameRectForContentRect_styleMask(frameRect, window.styleMask()),
                true, window.isVisible());
    }

    protected double getMinWindowHeight() {
        final NSRect contentRect = this.getContentRect();
        //Border top + toolbar
        return contentRect.size.height.doubleValue()
                + 40 + toolbarHeightForWindow(window);
    }

    protected double getMinWindowWidth() {
        final NSRect contentRect = this.getContentRect();
        return contentRect.size.width.doubleValue();
    }

    protected static double toolbarHeightForWindow(final NSWindow window) {
        NSRect windowFrame = NSWindow.contentRectForFrameRect_styleMask(window.frame(), window.styleMask());
        return windowFrame.size.height.doubleValue() - window.contentView().frame().size.height.doubleValue();
    }

    /**
     * @return Minimum size to fit content view of currently selected tab.
     */
    protected NSRect getContentRect() {
        return window.contentView().frame();
    }

    /**
     * @param toggle Checkbox
     * @param select Selected
     */
    protected void setState(final NSButton toggle, final boolean select) {
        if(select && NSCell.NSOffState == toggle.state()
                || !select && NSCell.NSOnState == toggle.state()) {
            // Synchronize state
            toggle.performClick(null);
        }
        toggle.setState(select ? NSCell.NSOnState : NSCell.NSOffState);
    }

    /**
     * Display alert as sheet to the window of this controller
     *
     * @param alert    Sheet
     * @param callback Dismissed notification
     */
    public void alert(final NSAlert alert, final SheetCallback callback) {
        final AlertController c = new AlertController(alert) {
            @Override
            public void callback(final int returncode) {
                callback.callback(returncode);
            }
        };
        c.beginSheet(this);
    }

    @Action
    public void helpButtonClicked(final ID sender) {
        BrowserLauncherFactory.get().open(ProviderHelpServiceFactory.get().help());
    }

    protected void print(NSView view) {
        NSPrintInfo print = NSPrintInfo.sharedPrintInfo();
        print.setOrientation(NSPrintInfo.NSPrintingOrientation.NSLandscapeOrientation);
        NSPrintOperation op = NSPrintOperation.printOperationWithView_printInfo(view, print);
        op.setShowsPrintPanel(true);
        final NSPrintPanel panel = op.printPanel();
        panel.setOptions(panel.options() | NSPrintPanel.NSPrintPanelShowsOrientation
                | NSPrintPanel.NSPrintPanelShowsPaperSize | NSPrintPanel.NSPrintPanelShowsScaling);
        op.runOperationModalForWindow_delegate_didRunSelector_contextInfo(window, this.id(),
                Foundation.selector("printOperationDidRun:success:contextInfo:"), null);
    }

    public void printOperationDidRun_success_contextInfo(NSPrintOperation op, boolean success, ID contextInfo) {
        if(!success) {
            log.warn("Printing failed for context {}", contextInfo);
        }
    }
}
