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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.delegate.MenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.TransferMenuDelegate;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @version $Id$
 */
public class CDProgressController extends CDBundleController {
    private static Logger log = Logger.getLogger(CDProgressController.class);

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

    public CDProgressController(final Transfer transfer) {
        this.transfer = transfer;
        this.meter = new Speedometer(transfer);
        this.init();
    }

    private ProgressListener pl;

    private TransferListener tl;

    @Override
    protected void invalidate() {
        transfer.getSession().removeProgressListener(pl);
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
        this.transfer.getSession().addProgressListener(this.pl = new ProgressListener() {
            public void message(final String message) {
                messageText = message;
                invoke(new DefaultMainAction() {
                    public void run() {
                        setMessageText();
                    }
                });
            }
        });
        this.transfer.addListener(this.tl = new TransferAdapter() {
            /**
             * Timer to update the progress indicator
             */
            private ScheduledFuture progressTimer;

            final static long delay = 0;
            final static long period = 500; //in milliseconds

            @Override
            public void transferWillStart() {
                invoke(new DefaultMainAction() {
                    public void run() {
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
                    public void run() {
                        progressBar.stopAnimation(null);
                        progressBar.setIndeterminate(true);
                        progressBar.setHidden(true);
                        messageText = null;
                        // Do not display any progress text when transfer is stopped
                        final Date timestamp = transfer.getTimestamp();
                        if(null != timestamp) {
                            messageText = CDDateFormatter.getLongFormat(timestamp.getTime());
                        }
                        setMessageText();
                        setProgressText();
                        setStatusText();
                        statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
                        filesPopup.itemAtIndex(0).setEnabled(transfer.getRoot().getLocal().exists());
                    }
                });
            }

            @Override
            public void willTransferPath(final Path path) {
                meter.reset();
                progressTimer = getTimerPool().scheduleAtFixedRate(new Runnable() {
                    public void run() {
                        invoke(new DefaultMainAction() {
                            public void run() {
                                setProgressText();
                                final double transferred = transfer.getTransferred();
                                if(transferred > 0) {
                                    progressBar.setIndeterminate(false);
                                }
                                progressBar.setMaxValue(transfer.getSize());
                                progressBar.setDoubleValue(transferred);
                            }
                        });
                    }
                }, delay, period, TimeUnit.MILLISECONDS);
            }

            @Override
            public void didTransferPath(final Path path) {
                progressTimer.cancel(false);
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
        StringBuffer b = new StringBuffer();
        if(messageText != null) {
            b.append(messageText);
        }
        messageField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(b.toString(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private void setProgressText() {
        progressField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(meter.getProgress(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private void setStatusText() {
        StringBuffer b = new StringBuffer();
        if(!transfer.isRunning()) {
            if(transfer instanceof DownloadTransfer) {
                b.append(transfer.isComplete() ? Locale.localizedString("Download complete", "Growl") :
                        Locale.localizedString("Transfer incomplete", "Status"));
            }
            if(transfer instanceof UploadTransfer) {
                b.append(transfer.isComplete() ? Locale.localizedString("Upload complete", "Growl") :
                        Locale.localizedString("Transfer incomplete", "Status"));
            }
            if(transfer instanceof SyncTransfer) {
                b.append(transfer.isComplete() ? Locale.localizedString("Synchronization complete", "Growl") :
                        Locale.localizedString("Transfer incomplete", "Status"));
            }
        }
        statusField.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                b.toString(),
                TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private static final NSDictionary NORMAL_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(
                    NSFont.systemFontOfSize(NSFont.smallSystemFontSize()),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL),
            NSArray.arrayWithObjects(
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    private static final NSDictionary HIGHLIGHTED_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(
                    NSFont.systemFontOfSize(NSFont.smallSystemFontSize()),
                    NSColor.whiteColor(),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL),
            NSArray.arrayWithObjects(
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    private static final NSDictionary DARK_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(
                    NSFont.systemFontOfSize(NSFont.smallSystemFontSize()),
                    NSColor.darkGrayColor(),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL),
            NSArray.arrayWithObjects(
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    public void setHighlighted(final boolean highlighted) {
        statusField.setTextColor(highlighted ? NSColor.whiteColor() : NSColor.textColor());
        progressField.setTextColor(highlighted ? NSColor.whiteColor() : NSColor.darkGrayColor());
        messageField.setTextColor(highlighted ? NSColor.whiteColor() : NSColor.darkGrayColor());
        if(transfer.getRoot().getLocal().exists()) {
            filesPopup.itemAtIndex(0).setAttributedTitle(
                    NSAttributedString.attributedStringWithAttributes(filesPopup.itemAtIndex(0).title(),
                            highlighted ? HIGHLIGHTED_FONT_ATTRIBUTES : NORMAL_FONT_ATTRIBUTES)
            );
        }
        else {
            filesPopup.itemAtIndex(0).setAttributedTitle(
                    NSAttributedString.attributedStringWithAttributes(filesPopup.itemAtIndex(0).title(),
                            highlighted ? HIGHLIGHTED_FONT_ATTRIBUTES : DARK_FONT_ATTRIBUTES)
            );
        }
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    @Outlet
    private NSPopUpButton filesPopup;

    private MenuDelegate filesPopupMenuDelegate;

    public void setFilesPopup(NSPopUpButton filesPopup) {
        this.filesPopup = filesPopup;
        this.filesPopup.setTarget(this.id());
        this.filesPopup.removeAllItems();
        {
            Path path = transfer.getRoot();
            NSMenuItem item = this.filesPopup.menu().addItemWithTitle_action_keyEquivalent(path.getName(), Foundation.selector("reveal:"), "");
            item.setRepresentedObject(path.getAbsolute());
            item.setImage(CDIconCache.instance().iconForPath(path, 16));
            item.setEnabled(path.getLocal().exists());
        }
        this.filesPopupMenuDelegate = new TransferMenuDelegate(transfer.getRoots());
        this.filesPopup.menu().setDelegate(this.filesPopupMenuDelegate.id());
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

    private static final NSImage RED_ICON = CDIconCache.iconNamed("statusRed.tiff");
    private static final NSImage GREEN_ICON = CDIconCache.iconNamed("statusGreen.tiff");
    private static final NSImage YELLOW_ICON = CDIconCache.iconNamed("statusYellow.tiff");

    public void setStatusIconView(final NSImageView statusIconView) {
        this.statusIconView = statusIconView;
        this.statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
    }

    @Outlet
    private NSImageView iconImageView;

    public void setIconImageView(final NSImageView iconImageView) {
        this.iconImageView = iconImageView;
        if(transfer instanceof DownloadTransfer) {
            iconImageView.setImage(CDIconCache.iconNamed("arrowDown", 32));
        }
        else if(transfer instanceof UploadTransfer) {
            iconImageView.setImage(CDIconCache.iconNamed("arrowUp", 32));
        }
        else if(transfer instanceof SyncTransfer) {
            iconImageView.setImage(CDIconCache.iconNamed("sync", 32));
        }
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