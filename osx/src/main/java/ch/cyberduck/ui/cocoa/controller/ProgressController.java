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
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSFont;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSImageView;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSProgressIndicator;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.formatter.SizeFormatter;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.ui.cocoa.delegate.AbstractMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.TransferMenuDelegate;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

public class ProgressController extends BundleController implements TransferListener, ProgressListener {

    private static final NSImage RED_ICON = IconCacheFactory.<NSImage>get().iconNamed("NSStatusUnavailable");
    private static final NSImage GREEN_ICON = IconCacheFactory.<NSImage>get().iconNamed("NSStatusAvailable");
    private static final NSImage YELLOW_ICON = IconCacheFactory.<NSImage>get().iconNamed("NSStatusPartiallyAvailable");

    private final Transfer transfer;
    /**
     * Formatter for file size
     */
    private final SizeFormatter sizeFormatter = SizeFormatterFactory.get();

    @Outlet
    private NSPopUpButton filesPopup;
    @Delegate
    private AbstractMenuDelegate filesPopupMenuDelegate;
    @Outlet
    private NSTextField progressField;
    @Outlet
    private NSTextField statusField;
    @Outlet
    private NSTextField messageField;
    @Outlet
    private NSProgressIndicator progressBar;
    @Outlet
    private NSImageView statusIconView;
    @Outlet
    private NSImageView iconImageView;
    /**
     * The view drawn in the table cell
     */
    @Outlet
    private NSView progressView;

    public ProgressController(final Transfer transfer) {
        this.transfer = transfer;
        this.loadBundle();
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    /**
     * Resets both the progress and status field
     */
    @Override
    public void awakeFromNib() {
        this.setProgress(MessageFormat.format(LocaleFactory.localizedString("{0} of {1}"),
                sizeFormatter.format(transfer.getTransferred()),
                sizeFormatter.format(transfer.getSize())));
        this.setMessage(StringUtils.EMPTY);
        this.setStatus(LocaleFactory.localizedString(transfer.isComplete() ?
                String.format("%s complete", StringUtils.capitalize(transfer.getType().name())) : "Transfer incomplete", "Status"));
        super.awakeFromNib();
    }

    @Override
    protected String getBundleName() {
        return "Progress.nib";
    }

    @Override
    public void message(final String message) {
        this.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                setMessage(message);
            }
        });
    }

    @Override
    public void transferDidStart(final Transfer transfer) {
        invoke(new DefaultMainAction() {
            @Override
            public void run() {
                progressBar.setHidden(false);
                progressBar.setIndeterminate(true);
                progressBar.startAnimation(null);
                statusIconView.setImage(YELLOW_ICON);
                setProgress(StringUtils.EMPTY);
                setStatus(StringUtils.EMPTY);
            }
        });
    }

    @Override
    public void transferDidStop(final Transfer transfer) {
        invoke(new DefaultMainAction() {
            @Override
            public void run() {
                progressBar.stopAnimation(null);
                progressBar.setIndeterminate(true);
                progressBar.setHidden(true);
                setMessage(StringUtils.EMPTY);
                setProgress(MessageFormat.format(LocaleFactory.localizedString("{0} of {1}"),
                        sizeFormatter.format(transfer.getTransferred()),
                        sizeFormatter.format(transfer.getSize())));
                setStatus(LocaleFactory.localizedString(LocaleFactory.localizedString(transfer.isComplete() ?
                        String.format("%s complete", StringUtils.capitalize(transfer.getType().name())) : "Transfer incomplete", "Status"), "Status"));
                statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
            }
        });
    }

    @Override
    public void transferDidProgress(final Transfer transfer, final TransferProgress progress) {
        this.setProgress(progress.getProgress());
        final double transferred = progress.getTransferred();
        final double size = progress.getSize();
        invoke(new DefaultMainAction() {
            @Override
            public void run() {
                if(transfer.isComplete()) {
                    progressBar.setIndeterminate(true);
                }
                else {
                    if(progress.isComplete()) {
                        progressBar.setIndeterminate(true);
                    }
                    else {
                        progressBar.setIndeterminate(false);
                        progressBar.setMaxValue(size);
                        progressBar.setDoubleValue(transferred);
                    }
                }
            }
        });
    }

    private void setProgress(final String message) {
        this.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                progressField.setStringValue(message);
            }
        });
    }

    public void setMessage(final String message) {
        final String text;
        if(StringUtils.isBlank(message)) {
            // Do not display any progress text when transfer is stopped
            final Date timestamp = transfer.getTimestamp();
            if(null != timestamp) {
                text = UserDateFormatterFactory.get().getLongFormat(timestamp.getTime(), false);
            }
            else {
                text = StringUtils.EMPTY;
            }
        }
        else {
            text = message;
        }
        messageField.setStringValue(text);
    }

    private void setStatus(final String status) {
        statusField.setStringValue(status);
    }

    @Outlet
    public void setFilesPopup(final NSPopUpButton p) {
        this.filesPopup = p;
        this.filesPopup.setTarget(this.id());
        this.filesPopup.removeAllItems();
        final List<TransferItem> items = transfer.getRoots();
        for(int i = 0; i < items.size(); i++) {
            final TransferItem entry = items.get(i);
            this.filesPopup.addItemWithTitle(i == 0 && items.size() > 1 ?
                    String.format("%s (%d more)", entry.remote.getName(), items.size() - 1) : entry.remote.getName());
        }
        this.filesPopupMenuDelegate = new TransferMenuDelegate(transfer);
        this.filesPopup.menu().setDelegate(this.filesPopupMenuDelegate.id());
    }

    @Outlet
    public void setProgressField(final NSTextField f) {
        this.progressField = f;
        this.progressField.setEditable(false);
        this.progressField.setSelectable(false);
        this.progressField.setFont(NSFont.monospacedDigitSystemFontOfSize(NSFont.smallSystemFontSize()));
    }

    @Outlet
    public void setStatusField(final NSTextField f) {
        this.statusField = f;
        this.statusField.setEditable(false);
        this.statusField.setSelectable(false);
    }

    @Outlet
    public void setMessageField(final NSTextField f) {
        this.messageField = f;
        this.messageField.setEditable(false);
        this.messageField.setSelectable(false);
    }

    @Outlet
    public void setProgressBar(final NSProgressIndicator p) {
        this.progressBar = p;
        this.progressBar.setDisplayedWhenStopped(false);
        this.progressBar.setUsesThreadedAnimation(true);
        this.progressBar.setControlSize(NSCell.NSSmallControlSize);
        this.progressBar.setStyle(NSProgressIndicator.NSProgressIndicatorBarStyle);
        this.progressBar.setMinValue(0);
    }

    @Outlet
    public void setStatusIconView(final NSImageView statusIconView) {
        this.statusIconView = statusIconView;
        this.statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
    }

    @Outlet
    public void setIconImageView(final NSImageView iconImageView) {
        this.iconImageView = iconImageView;
        if(!Factory.Platform.osversion.matches("(10)\\..*")) {
            switch(transfer.getType()) {
                case download:
                    this.iconImageView.setImage(IconCacheFactory.<NSImage>get().iconNamed("square.and.arrow.down", 64));
                    break;
                case upload:
                    this.iconImageView.setImage(IconCacheFactory.<NSImage>get().iconNamed("square.and.arrow.up", 64));
                    break;
                case sync:
                    this.iconImageView.setImage(IconCacheFactory.<NSImage>get().iconNamed("arrow.up.and.down.square", 64));
                    break;
                case copy:
                case move:
                    this.iconImageView.setImage(IconCacheFactory.<NSImage>get().iconNamed("arrow.left.and.right.square", 64));
                    break;
            }
        }
        else {
            this.iconImageView.setImage(IconCacheFactory.<NSImage>get().iconNamed(String.format("transfer-%s.tiff", transfer.getType().name()), 32));
        }
    }

    @Outlet
    public void setProgressView(final NSView v) {
        this.progressView = v;
    }

    @Override
    public NSView view() {
        return this.progressView;
    }
}
