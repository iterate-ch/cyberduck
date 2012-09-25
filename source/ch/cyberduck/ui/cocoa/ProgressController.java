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
import ch.cyberduck.core.Speedometer;
import ch.cyberduck.core.Transfer;
import ch.cyberduck.core.TransferAdapter;
import ch.cyberduck.core.TransferListener;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.ui.DateFormatterFactory;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.delegate.AbstractMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.TransferMenuDelegate;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSNotificationCenter;

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

    /**
     *
     */
    private Transfer transfer;

    /**
     * Keeping track of the current transfer rate
     */
    private Speedometer meter;

    /**
     * The current connection status message
     *
     * @see ch.cyberduck.core.ProgressListener#message(String)
     */
    private String messageText;

    public ProgressController(final Transfer transfer) {
        this.transfer = transfer;
        this.meter = new Speedometer(transfer);
        this.init();
    }

    private ProgressListener pl;

    private TransferListener tl;

    @Override
    protected void invalidate() {
        for(Session s : transfer.getSessions()) {
            s.removeProgressListener(pl);
        }
        transfer.removeListener(tl);
        filesPopup.menu().setDelegate(null);
        super.invalidate();
    }

    @Override
    protected String getBundleName() {
        return "Progress.nib";
    }

    private void init() {
        this.loadBundle();
        this.transfer.addListener(tl = new TransferAdapter() {
            /**
             * Timer to update the progress indicator
             */
            private ScheduledFuture progressTimer;

            final static long delay = 0;
            final static long period = 200; //in milliseconds

            @Override
            public void transferWillStart() {
                invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        pl = new ProgressListener() {
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
                            s.addProgressListener(pl);
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
                            s.removeProgressListener(pl);
                        }
                        progressBar.stopAnimation(null);
                        progressBar.setIndeterminate(true);
                        progressBar.setHidden(true);
                        messageText = null;
                        setMessageText();
                        setProgressText();
                        setStatusText();
                        statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
                        filesPopup.itemAtIndex(new NSInteger(0)).setEnabled(transfer.getRoot().getLocal().exists());
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
                messageText = DateFormatterFactory.instance().getLongFormat(timestamp.getTime());
            }
        }
        if(messageText != null) {
            b.append(messageText);
        }
        messageField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(b.toString(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private void setProgressText() {
        progressField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(meter.getProgress(), TRUNCATE_MIDDLE_ATTRIBUTES));
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

    public void setHighlighted(final boolean highlighted) {
        statusField.setTextColor(highlighted ? NSColor.whiteColor() : NSColor.textColor());
        progressField.setTextColor(highlighted ? NSColor.whiteColor() : NSColor.darkGrayColor());
        messageField.setTextColor(highlighted ? NSColor.whiteColor() : NSColor.darkGrayColor());
        this.setMenuHighlighted(highlighted);
        this.highlighted = highlighted;
    }

    private void setMenuHighlighted(boolean highlighted) {
        filesPopup.itemAtIndex(new NSInteger(0)).setAttributedTitle(
                NSAttributedString.attributedStringWithAttributes(filesPopup.itemAtIndex(new NSInteger(0)).title(),
                        highlighted ? HIGHLIGHTED_FONT_ATTRIBUTES : transfer.getRoot().getLocal().exists() ? NORMAL_FONT_ATTRIBUTES : DARK_FONT_ATTRIBUTES)
        );
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    @Outlet
    private NSPopUpButton filesPopup;

    private AbstractMenuDelegate filesPopupMenuDelegate;

    public void setFilesPopup(NSPopUpButton filesPopup) {
        this.filesPopup = filesPopup;
        this.filesPopup.setTarget(this.id());
        this.filesPopup.removeAllItems();
        {
            Path path = transfer.getRoot();
            NSMenuItem item = this.filesPopup.menu().addItemWithTitle_action_keyEquivalent(path.getName(), Foundation.selector("reveal:"), StringUtils.EMPTY);
            item.setRepresentedObject(path.getAbsolute());
            item.setImage(IconCache.instance().iconForPath(path, 16, false));
            item.setEnabled(path.getLocal().exists());
        }
        this.filesPopupMenuDelegate = new TransferMenuDelegate(transfer.getRoots());
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
        this.setMenuHighlighted(true);
    }

    @Outlet
    private NSTextField progressField;

    public void setProgressField(final NSTextField progressField) {
        this.progressField = progressField;
        this.progressField.setEditable(false);
        this.progressField.setSelectable(false);
        this.progressField.setTextColor(NSColor.darkGrayColor());
    }

    @Outlet
    private NSTextField statusField;

    public void setStatusField(final NSTextField statusField) {
        this.statusField = statusField;
        this.statusField.setEditable(false);
        this.statusField.setSelectable(false);
        this.statusField.setTextColor(NSColor.darkGrayColor());
    }

    @Outlet
    private NSTextField messageField;

    public void setMessageField(final NSTextField messageField) {
        this.messageField = messageField;
        this.messageField.setEditable(false);
        this.messageField.setSelectable(false);
        this.messageField.setTextColor(NSColor.darkGrayColor());
    }

    @Outlet
    private NSProgressIndicator progressBar;

    public void setProgressBar(final NSProgressIndicator progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setDisplayedWhenStopped(false);
        this.progressBar.setUsesThreadedAnimation(true);
        this.progressBar.setControlSize(NSCell.NSSmallControlSize);
        this.progressBar.setStyle(NSProgressIndicator.NSProgressIndicatorBarStyle);
        this.progressBar.setMinValue(0);
    }

    @Outlet
    private NSImageView statusIconView;

    private static final NSImage RED_ICON = IconCache.iconNamed("statusRed.tiff");
    private static final NSImage GREEN_ICON = IconCache.iconNamed("statusGreen.tiff");
    private static final NSImage YELLOW_ICON = IconCache.iconNamed("statusYellow.tiff");

    public void setStatusIconView(final NSImageView statusIconView) {
        this.statusIconView = statusIconView;
        this.statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
    }

    @Outlet
    private NSImageView iconImageView;

    public void setIconImageView(final NSImageView iconImageView) {
        this.iconImageView = iconImageView;
        this.iconImageView.setImage(IconCache.iconNamed(transfer.getImage(), 32));
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
