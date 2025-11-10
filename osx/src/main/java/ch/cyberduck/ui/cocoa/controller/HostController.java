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

import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSImageView;
import ch.cyberduck.binding.application.NSProgressIndicator;
import ch.cyberduck.binding.application.NSTableCellView;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSTokenField;
import ch.cyberduck.binding.foundation.NSMutableArray;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionListener;
import ch.cyberduck.ui.cocoa.view.BookmarkCell;

import org.apache.commons.lang3.StringUtils;

public class HostController extends BundleController implements BackgroundActionListener {

    private final BrowserController controller;
    private final Host host;

    @Outlet
    private NSTableCellView view;
    @Outlet
    private NSImageView bookmarkIconView;
    @Outlet
    private NSTextField nicknameField;
    @Outlet
    private NSTokenField labelsField;
    @Outlet
    private NSTextField hostnameField;
    @Outlet
    private NSTextField usernameField;
    @Outlet
    private NSImageView statusIconView;
    @Outlet
    private NSProgressIndicator progressIndicator;

    public HostController(final BrowserController controller, final Host host) {
        this.controller = controller;
        this.host = host;
        this.loadBundle();
    }

    @Override
    protected String getBundleName() {
        return "Host";
    }

    @Override
    public void awakeFromNib() {
        this.update();
        super.awakeFromNib();
    }

    @Outlet
    public void setBookmarkIconView(final NSImageView view) {
        this.bookmarkIconView = view;
    }

    @Outlet
    public void setNicknameField(final NSTextField field) {
        this.nicknameField = field;
    }

    @Outlet
    public void setLabelsField(final NSTokenField labelsField) {
        this.labelsField = labelsField;
    }

    @Outlet
    public void setHostnameField(final NSTextField field) {
        this.hostnameField = field;
    }

    @Outlet
    public void setUsernameField(final NSTextField field) {
        this.usernameField = field;
    }

    @Outlet
    public void setStatusIconView(final NSImageView view) {
        this.statusIconView = view;
    }

    @Outlet
    public void setProgressIndicator(final NSProgressIndicator indicator) {
        this.progressIndicator = indicator;
    }

    @Override
    public NSTableCellView view() {
        return view;
    }

    @Outlet
    public void setView(final NSTableCellView view) {
        this.view = view;
        this.view.setTranslatesAutoresizingMaskIntoConstraints(true);
        this.view.setIdentifier(host.getUuid());
    }

    /**
     * Update values in all controls
     */
    public void update() {
        final int size = PreferencesFactory.get().getInteger("bookmark.icon.size");
        bookmarkIconView.setImage(IconCacheFactory.<NSImage>get().iconNamed(host.getProtocol().disk(), size));
        this.updateField(nicknameField, BookmarkNameProvider.toString(host));
        final NSMutableArray labels = NSMutableArray.array();
        host.getLabels().forEach(labels::addObject);
        labelsField.setObjectValue(labels);
        hostnameField.setHidden(size == BookmarkCell.SMALL_BOOKMARK_SIZE);
        this.updateField(hostnameField, host.getHostname());
        usernameField.setHidden(size == BookmarkCell.SMALL_BOOKMARK_SIZE || size == BookmarkCell.MEDIUM_BOOKMARK_SIZE);
        this.updateField(usernameField, host.getCredentials().getUsername());
    }

    @Override
    public void start(final BackgroundAction<?> action) {
        progressIndicator.startAnimation(null);
        statusIconView.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSStatusPartiallyAvailable", 16));
    }

    @Override
    public void stop(final BackgroundAction<?> action) {
        if(controller.isConnected()) {
            statusIconView.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSStatusAvailable", 16));
        }
        else {
            statusIconView.setImage(null);
        }
        progressIndicator.stopAnimation(null);
    }
}
