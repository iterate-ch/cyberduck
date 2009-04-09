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

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSSelector;

import ch.cyberduck.core.*;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.ui.cocoa.delegate.MenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.TransferMenuDelegate;
import ch.cyberduck.ui.cocoa.threading.DefaultMainAction;

import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

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

    protected void invalidate() {
        this.transfer.getSession().removeProgressListener(pl);
        this.transfer.removeListener(tl);
    }

    protected String getBundleName() {
        return "Progress.nib";
    }

    private void init() {
        this.loadBundle();
        this.transfer.getSession().addProgressListener(this.pl = new ProgressListener() {
            public void message(final String message) {
                messageText = message;
                CDMainApplication.invoke(new DefaultMainAction() {
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
            private Timer progressTimer;

            final long delay = 0;
            final long period = 500; //in milliseconds

            public void transferWillStart() {
                progressBar.setIndeterminate(true);
                progressBar.setHidden(false);
                progressBar.startAnimation(null);
                CDMainApplication.invoke(new DefaultMainAction() {
                    public void run() {
                        statusIconView.setImage(YELLOW_ICON);
                        setProgressText();
                        setStatusText();
                    }
                });
            }

            public void transferDidEnd() {
                progressBar.stopAnimation(null);
                CDMainApplication.invoke(new DefaultMainAction() {
                    public void run() {
                        messageText = null;
                        // Do not display any progress text when transfer is stopped
                        final Date timestamp = transfer.getTimestamp();
                        if(null != timestamp) {
                            messageText = CDDateFormatter.getLongFormat(timestamp.getTime());
                        }
                        setMessageText();
                        setProgressText();
                        setStatusText();
                        progressBar.setHidden(true);
                        statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
                        filesPopup.itemAtIndex(0).setEnabled(transfer.getRoot().getLocal().exists());
                    }
                }, true);
            }

            public void willTransferPath(final Path path) {
                meter.reset();
                progressTimer = new Timer();
                progressTimer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        CDMainApplication.invoke(new DefaultMainAction() {
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
                }, delay, period);
            }

            public void didTransferPath(final Path path) {
                progressTimer.cancel();
                meter.reset();
            }

            public void bandwidthChanged(BandwidthThrottle bandwidth) {
                meter.reset();
            }
        });
    }

    /**
     * Resets both the progress and status field
     */
    public void awakeFromNib() {
        this.setProgressText();
        this.setMessageText();
        this.setStatusText();
    }

    private void setMessageText() {
        StringBuffer b = new StringBuffer();
        if(messageText != null) {
            b.append(messageText);
        }
        messageField.setAttributedStringValue(new NSAttributedString(b.toString(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private void setProgressText() {
        progressField.setAttributedStringValue(new NSAttributedString(meter.getProgress(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private void setStatusText() {
        StringBuffer b = new StringBuffer();
        if(!transfer.isRunning()) {
            if(transfer instanceof DownloadTransfer) {
                b.append(transfer.isComplete() ? NSBundle.localizedString("Download complete", "Growl", "") :
                        NSBundle.localizedString("Transfer incomplete", "Status", ""));
            }
            if(transfer instanceof UploadTransfer) {
                b.append(transfer.isComplete() ? NSBundle.localizedString("Upload complete", "Growl", "") :
                        NSBundle.localizedString("Transfer incomplete", "Status", ""));
            }
            if(transfer instanceof SyncTransfer) {
                b.append(transfer.isComplete() ? NSBundle.localizedString("Synchronization complete", "Growl", "") :
                        NSBundle.localizedString("Transfer incomplete", "Status", ""));
            }
        }
        statusField.setAttributedStringValue(new NSAttributedString(
                b.toString(),
                TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private static final NSDictionary NORMAL_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(NSFont.smallSystemFontSize()),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName}
    );

    private static final NSDictionary HIGHLIGHTED_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(NSFont.smallSystemFontSize()),
                    NSColor.whiteColor(),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName}
    );

    private static final NSDictionary DARK_FONT_ATTRIBUTES = new NSDictionary(
            new Object[]{
                    NSFont.systemFontOfSize(NSFont.smallSystemFontSize()),
                    NSColor.darkGrayColor(),
                    CDTableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL},
            new Object[]{
                    NSAttributedString.FontAttributeName,
                    NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName}
    );

    public void setHighlighted(final boolean highlighted) {
        statusField.setTextColor(highlighted ? NSColor.whiteColor() : NSColor.textColor());
        progressField.setTextColor(highlighted ? NSColor.whiteColor() : NSColor.darkGrayColor());
        messageField.setTextColor(highlighted ? NSColor.whiteColor() : NSColor.darkGrayColor());
        if(transfer.getRoot().getLocal().exists()) {
            filesPopup.itemAtIndex(0).setAttributedTitle(
                    new NSAttributedString(filesPopup.itemAtIndex(0).title(),
                            highlighted ? HIGHLIGHTED_FONT_ATTRIBUTES : NORMAL_FONT_ATTRIBUTES)
            );
        }
        else {
            filesPopup.itemAtIndex(0).setAttributedTitle(
                    new NSAttributedString(filesPopup.itemAtIndex(0).title(),
                            highlighted ? HIGHLIGHTED_FONT_ATTRIBUTES : DARK_FONT_ATTRIBUTES)
            );
        }
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSPopUpButton filesPopup; // IBOutlet

    private MenuDelegate filesPopupMenuDelegate;

    public void setFilesPopup(NSPopUpButton filesPopup) {
        this.filesPopup = filesPopup;
        this.filesPopup.setTarget(this);
        this.filesPopup.removeAllItems();
        {
            Path path = transfer.getRoot();
            NSMenuItem item = new NSMenuItem();
            item.setTitle(path.getName());
            item.setAction(new NSSelector("reveal", new Class[]{NSMenuItem.class}));
            item.setRepresentedObject(path);
            item.setImage(CDIconCache.instance().iconForPath(path, 16));
            item.setEnabled(path.getLocal().exists());
            this.filesPopup.menu().addItem(item);
        }
        this.filesPopup.menu().setDelegate(filesPopupMenuDelegate
                = new TransferMenuDelegate(transfer.getRoots())
        );
    }

    private NSTextField progressField; // IBOutlet

    public void setProgressField(final NSTextField progressField) {
        this.progressField = progressField;
        this.progressField.setEditable(false);
        this.progressField.setSelectable(false);
        this.progressField.setTextColor(NSColor.darkGrayColor());
    }

    private NSTextField statusField; // IBOutlet

    public void setStatusField(final NSTextField statusField) {
        this.statusField = statusField;
        this.statusField.setEditable(false);
        this.statusField.setSelectable(false);
        this.statusField.setTextColor(NSColor.darkGrayColor());
    }

    private NSTextField messageField; // IBOutlet

    public void setMessageField(final NSTextField messageField) {
        this.messageField = messageField;
        this.messageField.setEditable(false);
        this.messageField.setSelectable(false);
        this.messageField.setTextColor(NSColor.darkGrayColor());
    }

    private NSProgressIndicator progressBar; // IBOutlet

    public void setProgressBar(final NSProgressIndicator progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setDisplayedWhenStopped(false);
        this.progressBar.setUsesThreadedAnimation(true);
        this.progressBar.setControlTint(NSProgressIndicator.BlueControlTint);
        this.progressBar.setControlSize(NSProgressIndicator.SmallControlSize);
        this.progressBar.setStyle(NSProgressIndicator.ProgressIndicatorBarStyle);
        this.progressBar.setMinValue(0);
    }

    private NSImageView statusIconView; //IBOutlet

    private static final NSImage RED_ICON = NSImage.imageNamed("statusRed.tiff");
    private static final NSImage GREEN_ICON = NSImage.imageNamed("statusGreen.tiff");
    private static final NSImage YELLOW_ICON = NSImage.imageNamed("statusYellow.tiff");

    public void setStatusIconView(final NSImageView statusIconView) {
        this.statusIconView = statusIconView;
        this.statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
    }

    private NSImageView iconImageView; //IBOutlet

    public void setIconImageView(final NSImageView iconImageView) {
        this.iconImageView = iconImageView;
        if(transfer instanceof DownloadTransfer) {
            iconImageView.setImage(CDIconCache.instance().iconForName("arrowDown", 32));
        }
        else if(transfer instanceof UploadTransfer) {
            iconImageView.setImage(CDIconCache.instance().iconForName("arrowUp", 32));
        }
        else if(transfer instanceof SyncTransfer) {
            iconImageView.setImage(CDIconCache.instance().iconForName("sync", 32));
        }
    }

    /**
     * The view drawn in the table cell
     */
    private NSView progressView; // IBOutlet

    public void setProgressView(final NSView v) {
        this.progressView = v;
    }

    public NSView view() {
        return this.progressView;
    }
}