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
import ch.cyberduck.binding.SheetController;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.local.BrowserLauncherFactory;

import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSSize;

public abstract class BookmarkContainerController extends SheetController {

    private final Host bookmark;
    private final BookmarkController proxy;

    /**
     * Container View
     */
    @Outlet
    private NSView contentView;
    /**
     * Container View
     */
    @Outlet
    private NSView optionsView;

    public BookmarkContainerController(final Host bookmark, final BookmarkController proxy) {
        this.bookmark = bookmark;
        this.proxy = proxy;
    }

    @Override
    public void loadBundle() {
        proxy.loadBundle();
        super.loadBundle();
    }

    @Override
    public void awakeFromNib() {
        proxy.awakeFromNib();
        proxy.setWindow(window);
        this.addSubview(contentView, proxy.getContentView());
        if(optionsView != null) {
            final NSView additionalOptions = proxy.getOptionsView();
            if(additionalOptions != null) {
                this.addSubview(additionalOptions, optionsView);
            }
        }
        this.resize();
        proxy.focus(window);
        proxy.addObserver(bookmark -> this.resize());
        super.awakeFromNib();
    }

    private void addSubview(final NSView parent, final NSView subview) {
        subview.setTranslatesAutoresizingMaskIntoConstraints(true);
        subview.setFrame(parent.bounds());
        parent.addSubview(subview);
    }

    public void setContentView(final NSView view) {
        this.contentView = view;
        this.contentView.setTranslatesAutoresizingMaskIntoConstraints(true);
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

    public void update() {
        proxy.update();
    }

    public void addObserver(final BookmarkController.BookmarkObserver observer) {
        proxy.addObserver(observer);
    }
}
