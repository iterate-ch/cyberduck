package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.local.BrowserLauncherFactory;

import ch.cyberduck.ui.LoginInputValidator;

import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSSize;

public abstract class BookmarkContainerController extends DefaultBookmarkController {

    private final Host bookmark;

    /**
     * Container View
     */
    @Outlet
    private NSView containerContentView;
    /**
     * Container View
     */
    @Outlet
    private NSView optionsView;

    public BookmarkContainerController(final Host bookmark) {
        super(bookmark);
        this.bookmark = bookmark;
    }

    public BookmarkContainerController(final Host bookmark, final LoginOptions options) {
        super(bookmark, options);
        this.bookmark = bookmark;
    }

    public BookmarkContainerController(final Host bookmark, final LoginInputValidator validator, final LoginOptions options) {
        super(bookmark, validator, options);
        this.bookmark = bookmark;
    }

    @Override
    public void awakeFromNib() {
        super.awakeFromNib();
        final NSView contentView = this.getContentView();
        this.addSubview(containerContentView, contentView);
        if(optionsView != null) {
            final NSView containerOptionsView = this.getContainerOptionsView();
            if(containerOptionsView != null) {
                this.addSubview(containerOptionsView, optionsView);
            }
        }
        this.resize();
        this.focus(window);
    }

    @Override
    public void protocolSelectionChanged(final NSPopUpButton sender) {
        super.protocolSelectionChanged(sender);
        this.resize();
    }

    public void setContainerContentView(final NSView view) {
        this.containerContentView = view;
        this.containerContentView.setTranslatesAutoresizingMaskIntoConstraints(true);
    }

    public void setOptionsView(final NSView view) {
        this.optionsView = view;
        this.optionsView.setTranslatesAutoresizingMaskIntoConstraints(true);
    }

    public Host getBookmark() {
        return bookmark;
    }

    @Delegate
    public NSSize windowWillResize_toSize(final NSWindow window, final NSSize newSize) {
        // Only allow horizontal sizing
        return new NSSize(newSize.width.doubleValue(), window.frame().size.height.doubleValue());
    }

    @Override
    protected double getMinWindowHeight() {
        return 0d;
    }

    @Override
    protected double getMinWindowWidth() {
        return 0d;
    }

    @Override
    @Action
    public void helpButtonClicked(final ID sender) {
        BrowserLauncherFactory.get().open(ProviderHelpServiceFactory.get().help(bookmark.getProtocol()));
    }
}
