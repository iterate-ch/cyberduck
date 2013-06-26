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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.date.UserDateFormatterFactory;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAdapter;
import ch.cyberduck.core.transfer.TransferListener;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.delegate.AbstractMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.TransferMenuDelegate;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSNotificationCenter;
import ch.cyberduck.ui.resources.IconCacheFactory;

import org.apache.commons.lang.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @version $Id$
 */
public class ProgressController extends BundleController {

    private Transfer transfer;

    /**
     * Keeping track of the current transfer rate
     */
    private TransferSpeedometer meter;

    /**
     * The current connection status message
     *
     * @see ch.cyberduck.core.ProgressListener#message(String)
     */
    private String messageText;

    public ProgressController(final Transfer transfer) {
        this.transfer = transfer;
        this.meter = new TransferSpeedometer(transfer);
        this.init();
    }

    private ProgressListener progressListener;

    private TransferListener transferListener;

    @Override
    protected void invalidate() {
        transfer.removeListener(transferListener);
        filesPopup.menu().setDelegate(null);
        super.invalidate();
    }

    @Override
    protected String getBundleName() {
        return "Progress.nib";
    }

    private void init() {
        this.loadBundle();
        this.transfer.addListener(transferListener = new TransferAdapter() {
            /**
             * Timer to update the progress indicator
             */
            private ScheduledFuture progressTimer;

            final static long delay = 0;
            final static long period = 500; //in milliseconds

            @Override
            public void transferWillStart() {
                invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        progressListener = new ProgressListener() {
                            @Override
                            public void message(final String message) {
                                messageText = message;
                                invoke(new DefaultMainAction() {
                                    @Override
                                    public void run() {
                                        setMessageText();
                                    }
                                });
                            }
                        };
                        for(Session s : transfer.getSessions()) {
                            s.addProgressListener(progressListener);
                        }
                        progressBar.setHidden(false);
                        progressBar.setIndeterminate(true);
                        progressBar.startAnimation(null);
                        statusIconView.setImage(YELLOW_ICON);
                        setProgressText();
                        setStatusText();
                    }
                });
            }

            @Override
            public void transferDidEnd() {
                invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        for(Session s : transfer.getSessions()) {
                            s.removeProgressListener(progressListener);
                        }
                        progressBar.stopAnimation(null);
                        progressBar.setIndeterminate(true);
                        progressBar.setHidden(true);
                        messageText = null;
                        setMessageText();
                        setProgressText();
                        setStatusText();
                        statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
                    }
                });
            }

            @Override
            public void willTransferPath(final Path path) {
                meter.reset();
                progressTimer = getTimerPool().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        invoke(new DefaultMainAction() {
                            @Override
                            public void run() {
                                setProgressText();
                                final double transferred = transfer.getTransferred();
                                final double size = transfer.getSize();
                                if(transferred > 0 && size > 0) {
                                    progressBar.setIndeterminate(false);
                                    progressBar.setMaxValue(size);
                                    progressBar.setDoubleValue(transferred);
                                }
                            }
                        });
                    }
                }, delay, period, TimeUnit.MILLISECONDS);
            }

            @Override
            public void didTransferPath(final Path path) {
                boolean canceled = false;
                while(!canceled) {
                    canceled = progressTimer.cancel(false);
                }
                meter.reset();
            }

            @Override
            public void bandwidthChanged(BandwidthThrottle bandwidth) {
                meter.reset();
            }
        });
    }

    /**
     * Resets both the progress and status field
     */
    @Override
    public void awakeFromNib() {
        this.setProgressText();
        this.setMessageText();
        this.setStatusText();

        super.awakeFromNib();
    }

    private void setMessageText() {
        StringBuilder b = new StringBuilder();
        if(null == messageText) {
            // Do not display any progress text when transfer is stopped
            final Date timestamp = transfer.getTimestamp();
            if(null != timestamp) {
                messageText = UserDateFormatterFactory.get().getLongFormat(timestamp.getTime(), false);
            }
        }
        if(messageText != null) {
            b.append(messageText);
        }
        messageField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                b.toString(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private void setProgressText() {
        progressField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                meter.getProgress(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private void setStatusText() {
        statusField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                transfer.isRunning() ? StringUtils.EMPTY : Locale.localizedString(transfer.getStatus(), "Status"),
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

    private static final NSDictionary DARK_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(
                    NSFont.systemFontOfSize(NSFont.smallSystemFontSize()),
                    NSColor.darkGrayColor(),
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

    private AbstractMenuDelegate filesPopupMenuDelegate;

    public void setFilesPopup(final NSPopUpButton p) {
        this.filesPopup = p;
        this.filesPopup.setTarget(this.id());
        this.filesPopup.removeAllItems();
        for(Path path : transfer.getRoots()) {
            NSMenuItem item = this.filesPopup.menu().addItemWithTitle_action_keyEquivalent(path.getName(), Foundation.selector("reveal:"), StringUtils.EMPTY);
            item.setRepresentedObject(path.getAbsolute());
            item.setImage(IconCacheFactory.<NSImage>get().fileIcon(path, 16));
        }
        this.filesPopupMenuDelegate = new TransferMenuDelegate(transfer);
        this.filesPopup.menu().setDelegate(this.filesPopupMenuDelegate.id());
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("filesPopupWillShow:"),
                NSPopUpButton.PopUpButtonWillPopUpNotification,
                this.filesPopup);
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
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
        this.iconImageView.setImage(IconCacheFactory.<NSImage>get().iconNamed(transfer.getImage(), 32));
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
