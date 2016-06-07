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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSColor;
import ch.cyberduck.binding.application.NSFont;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSImageView;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSProgressIndicator;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
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
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSInteger;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

public class ProgressController extends BundleController implements TransferListener, ProgressListener {

    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

    private Transfer transfer;

    /**
     * Formatter for file size
     */
    private SizeFormatter sizeFormatter = SizeFormatterFactory.get();

    public ProgressController(final Transfer transfer) {
        this.transfer = transfer;
        this.loadBundle();
    }

    /**
     * Resets both the progress and status field
     */
    @Override
    public void awakeFromNib() {
        this.progress(MessageFormat.format(LocaleFactory.localizedString("{0} of {1}"),
                sizeFormatter.format(transfer.getTransferred()),
                sizeFormatter.format(transfer.getSize())));
        this.message(StringUtils.EMPTY);
        this.status(LocaleFactory.localizedString(transfer.isComplete() ?
                String.format("%s complete", StringUtils.capitalize(transfer.getType().name())) : "Transfer incomplete", "Status"));
        super.awakeFromNib();
    }

    @Override
    protected String getBundleName() {
        return "Progress.nib";
    }

    @Override
    public void invalidate() {
        filesPopup.menu().setDelegate(null);
        notificationCenter.removeObserver(this.id());
        super.invalidate();
    }

    @Override
    public void start(final Transfer transfer) {
        invoke(new DefaultMainAction() {
            @Override
            public void run() {
                progressBar.setHidden(false);
                progressBar.setIndeterminate(true);
                progressBar.startAnimation(null);
                statusIconView.setImage(YELLOW_ICON);
                progress(StringUtils.EMPTY);
                status(StringUtils.EMPTY);
            }
        });
    }

    @Override
    public void stop(final Transfer transfer) {
        invoke(new DefaultMainAction() {
            @Override
            public void run() {
                progressBar.stopAnimation(null);
                progressBar.setIndeterminate(true);
                progressBar.setHidden(true);
                message(StringUtils.EMPTY);
                progress(MessageFormat.format(LocaleFactory.localizedString("{0} of {1}"),
                        sizeFormatter.format(transfer.getTransferred()),
                        sizeFormatter.format(transfer.getSize())));
                status(LocaleFactory.localizedString(LocaleFactory.localizedString(transfer.isComplete() ?
                        String.format("%s complete", StringUtils.capitalize(transfer.getType().name())) : "Transfer incomplete", "Status"), "Status"));
                statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
            }
        });
    }

    @Override
    public void progress(final TransferProgress progress) {
        this.progress(progress.getProgress());
        final double transferred = progress.getTransferred();
        final double size = progress.getSize();
        invoke(new DefaultMainAction() {
            @Override
            public void run() {
                if(transferred > 0 && size > 0) {
                    progressBar.setIndeterminate(false);
                    progressBar.setMaxValue(size);
                    progressBar.setDoubleValue(transferred);
                }
                else {
                    progressBar.setIndeterminate(true);
                }
            }
        });
    }

    private void progress(final String message) {
        this.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                progressField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                        message, TRUNCATE_MIDDLE_ATTRIBUTES));
            }
        });
    }

    @Override
    public void message(final String message) {
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
        messageField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                text, TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private void status(final String status) {
        statusField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(status,
                TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private static final NSDictionary NORMAL_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(
                    NSFont.systemFontOfSize(NSFont.smallSystemFontSize()),
                    TableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL),
            NSArray.arrayWithObjects(
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    private static final NSDictionary HIGHLIGHTED_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(
                    NSFont.systemFontOfSize(NSFont.smallSystemFontSize()),
                    NSColor.whiteColor(),
                    TableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL),
            NSArray.arrayWithObjects(
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    private boolean highlighted;

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(final boolean h) {
        highlighted = h;
        statusField.setTextColor(h ? NSColor.whiteColor() : NSColor.textColor());
        progressField.setTextColor(h ? NSColor.whiteColor() : NSColor.darkGrayColor());
        messageField.setTextColor(h ? NSColor.whiteColor() : NSColor.darkGrayColor());
        this.setMenuHighlighted(h);
    }

    private void setMenuHighlighted(boolean highlighted) {
        for(int i = 0; i < filesPopup.numberOfItems().intValue(); i++) {
            filesPopup.itemAtIndex(new NSInteger(i)).setAttributedTitle(
                    NSAttributedString.attributedStringWithAttributes(filesPopup.itemAtIndex(new NSInteger(i)).title(),
                            highlighted ? HIGHLIGHTED_FONT_ATTRIBUTES : NORMAL_FONT_ATTRIBUTES)
            );
        }
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    @Outlet
    private NSPopUpButton filesPopup;

    @Delegate
    private AbstractMenuDelegate filesPopupMenuDelegate;

    public void setFilesPopup(final NSPopUpButton p) {
        this.filesPopup = p;
        this.filesPopup.setTarget(this.id());
        this.filesPopup.removeAllItems();
        final List<TransferItem> items = transfer.getRoots();
        for(int i = 0; i < items.size(); i++) {
            final TransferItem entry = items.get(i);
            final NSMenuItem item = this.filesPopup.menu().addItemWithTitle_action_keyEquivalent(entry.remote.getName(), null, StringUtils.EMPTY);
            if(i == 0) {
                if(items.size() > 1) {
                    item.setTitle(String.format("%s (%d more)", entry.remote.getName(), items.size() - 1));
                }
                else {
                    item.setTitle(entry.remote.getName());
                }
            }
            else {
                item.setTitle(entry.remote.getName());
            }
        }
        this.filesPopupMenuDelegate = new TransferMenuDelegate(transfer);
        this.filesPopup.menu().setDelegate(this.filesPopupMenuDelegate.id());
        notificationCenter.addObserver(this.id(),
                Foundation.selector("filesPopupWillShow:"),
                NSPopUpButton.PopUpButtonWillPopUpNotification,
                this.filesPopup);
        notificationCenter.addObserver(this.id(),
                Foundation.selector("filesPopupWillHide:"),
                "NSMenuDidEndTrackingNotification",
                this.filesPopup.menu());
    }

    @Action
    public void filesPopupWillShow(final NSNotification sender) {
        this.setMenuHighlighted(false);
    }

    @Action
    public void filesPopupWillHide(final NSNotification sender) {
        this.setMenuHighlighted(highlighted);
    }

    @Outlet
    private NSTextField progressField;

    public void setProgressField(final NSTextField f) {
        this.progressField = f;
        this.progressField.setEditable(false);
        this.progressField.setSelectable(false);
        this.progressField.setTextColor(NSColor.darkGrayColor());
    }

    @Outlet
    private NSTextField statusField;

    public void setStatusField(final NSTextField f) {
        this.statusField = f;
        this.statusField.setEditable(false);
        this.statusField.setSelectable(false);
        this.statusField.setTextColor(NSColor.darkGrayColor());
    }

    @Outlet
    private NSTextField messageField;

    public void setMessageField(final NSTextField f) {
        this.messageField = f;
        this.messageField.setEditable(false);
        this.messageField.setSelectable(false);
        this.messageField.setTextColor(NSColor.darkGrayColor());
    }

    @Outlet
    private NSProgressIndicator progressBar;

    public void setProgressBar(final NSProgressIndicator p) {
        this.progressBar = p;
        this.progressBar.setDisplayedWhenStopped(false);
        this.progressBar.setUsesThreadedAnimation(true);
        this.progressBar.setControlSize(NSCell.NSSmallControlSize);
        this.progressBar.setStyle(NSProgressIndicator.NSProgressIndicatorBarStyle);
        this.progressBar.setMinValue(0);
    }

    @Outlet
    private NSImageView statusIconView;

    private static final NSImage RED_ICON = IconCacheFactory.<NSImage>get().iconNamed("statusRed.tiff");
    private static final NSImage GREEN_ICON = IconCacheFactory.<NSImage>get().iconNamed("statusGreen.tiff");
    private static final NSImage YELLOW_ICON = IconCacheFactory.<NSImage>get().iconNamed("statusYellow.tiff");

    public void setStatusIconView(final NSImageView statusIconView) {
        this.statusIconView = statusIconView;
        this.statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
    }

    @Outlet
    private NSImageView iconImageView;

    public void setIconImageView(final NSImageView iconImageView) {
        this.iconImageView = iconImageView;
        this.iconImageView.setImage(IconCacheFactory.<NSImage>get().iconNamed(String.format("transfer-%s.tiff", transfer.getType().name()), 32));
    }

    /**
     * The view drawn in the table cell
     */
    private NSView progressView;

    public void setProgressView(final NSView v) {
        this.progressView = v;
    }

    @Override
    public NSView view() {
        return this.progressView;
    }
}
