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
import ch.cyberduck.ui.cocoa.growl.Growl;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDProgressController extends CDController {
    private static Logger log = Logger.getLogger(CDProgressController.class);

    private String statusText;

    private NSTimer progressTimer;

    private static NSMutableParagraphStyle lineBreakByTruncatingMiddleParagraph = new NSMutableParagraphStyle();
    private static NSMutableParagraphStyle lineBreakByTruncatingTailParagraph = new NSMutableParagraphStyle();

    static {
        lineBreakByTruncatingMiddleParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingMiddle);
        lineBreakByTruncatingTailParagraph.setLineBreakMode(NSParagraphStyle.LineBreakByTruncatingTail);
    }

    private static final NSDictionary TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingMiddleParagraph},
            new Object[]{NSAttributedString.ParagraphStyleAttributeName});
    private static final NSDictionary TRUNCATE_TAIL_PARAGRAPH_DICTIONARY = new NSDictionary(new Object[]{lineBreakByTruncatingTailParagraph},
            new Object[]{NSAttributedString.ParagraphStyleAttributeName});

    private Transfer transfer;

    public CDProgressController(final Transfer transfer) {
        this.transfer = transfer;
        QueueCollection.instance().addListener(new CollectionListener() {
            public void collectionItemAdded(Object item) {
                ;
            }

            public void collectionItemRemoved(Object item) {
                if(item.equals(CDProgressController.this)) {
                    CDProgressController.this.invalidate();
                }
            }

            public void collectionItemChanged(Object item) {
                ;
            }
        });
        synchronized(NSApplication.sharedApplication()) {
            if(!NSApplication.loadNibNamed("Progress", this)) {
                log.fatal("Couldn't load Progress.nib");
            }
        }
        this.init();
    }

    private void init() {
        final ProgressListener pl = new ProgressListener() {
            public void message(final String message) {
                CDProgressController.this.invoke(new Runnable() {
                    public void run() {
                        statusText = message;
                        progressField.setAttributedStringValue(new NSAttributedString(getProgressText(),
                                TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                    }
                });
            }
        };
        final TransferListener tl = new TransferListener() {
            public void transferWillStart() {
                CDProgressController.this.invoke(new Runnable() {
                    public void run() {
                        progressBar.setHidden(false);
                        progressBar.setIndeterminate(true);
                        progressBar.startAnimation(null);
                        progressBar.setNeedsDisplay(true);
                        statusIconView.setImage(RED_ICON);
                    }
                });
                transfer.getSession().addProgressListener(pl);
            }

            public void transferPaused() {
                CDProgressController.this.invoke(new Runnable() {
                    public void run() {
                        statusIconView.setImage(YELLOW_ICON);
                    }
                });
                Growl.instance().notify("Transfer queued", transfer.getHost().getHostname());
            }

            public void transferResumed() {
                CDProgressController.this.invoke(new Runnable() {
                    public void run() {
                        statusIconView.setImage(RED_ICON);
                    }
                });
            }

            public void transferDidEnd() {
                CDProgressController.this.invoke(new Runnable() {
                    public void run() {
                        statusText = null;
                        progressField.setAttributedStringValue(new NSAttributedString(getProgressText(),
                                TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
                        progressBar.setIndeterminate(true);
                        progressBar.stopAnimation(null);
                        progressBar.setHidden(true);
                        statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
                    }
                });
                transfer.getSession().removeProgressListener(pl);
            }

            public void willTransferPath(final Path path) {
                meter = new Speedometer();
                progressTimer = new NSTimer(0.1, //seconds
                        CDProgressController.this, //target
                        new NSSelector("update", new Class[]{NSTimer.class}),
                        transfer, //userInfo
                        true); //repeating
                mainRunLoop.addTimerForMode(progressTimer,
                        NSRunLoop.DefaultRunLoopMode);
            }

            public void didTransferPath(final Path path) {
                progressTimer.invalidate();
                meter = null;
            }
        };
        this.transfer.addListener(tl);
    }

    public void awakeFromNib() {
        this.filenameField.setAttributedStringValue(new NSAttributedString(this.transfer.getName(),
                TRUNCATE_TAIL_PARAGRAPH_DICTIONARY));
        this.progressField.setAttributedStringValue(new NSAttributedString(
                this.getProgressText(),
                TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
    }

    /**
     * Called from the main run loop using a NSTimer #progressTimer
     *
     * @param t
     */
    public void update(final NSTimer t) {
        this.progressField.setAttributedStringValue(
                new NSAttributedString(this.getProgressText(),
                        TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
        if(transfer.isInitialized()) {
            if(transfer.getSize() != -1) {
                this.progressBar.setIndeterminate(false);
                this.progressBar.setMinValue(0);
                this.progressBar.setMaxValue(transfer.getSize());
                this.progressBar.setDoubleValue(transfer.getCurrent());
            }
        }
        else if(transfer.isRunning()) {
            this.progressBar.setIndeterminate(true);
        }
    }

    private Speedometer meter;

    private class Speedometer {
        //the time to start counting bytes transfered
        private long timestamp = System.currentTimeMillis();
        //initial data already transfered
        private double initialBytesTransfered = transfer.getCurrent();
        private double bytesTransferred = 0;

        /**
         * Returns the data transfer rate. The rate should depend on the transfer
         * rate timestamp.
         *
         * @return The bytes being processed per second
         */
        public float getSpeed() {
            bytesTransferred = transfer.getCurrent();
            if(bytesTransferred > initialBytesTransfered) {
                if(0 == initialBytesTransfered) {
                    initialBytesTransfered = bytesTransferred;
                    return -1;
                }
                // number of seconds data was actually transferred
                double elapsedSeconds = (System.currentTimeMillis() - timestamp) / 1000;
                if(elapsedSeconds > 1) {
                    // bytes per second
                    return (float) ((bytesTransferred - initialBytesTransfered) / (elapsedSeconds));
                }
            }
            return -1;
        }

        public double getBytesTransfered() {
            return bytesTransferred;
        }
    }

    private static final String SEC_REMAINING = NSBundle.localizedString("seconds remaining", "Status", "");
    private static final String MIN_REMAINING = NSBundle.localizedString("minutes remaining", "Status", "");

    private String getProgressText() {
        StringBuffer b = new StringBuffer();
        b.append(Status.getSizeAsString(this.transfer.getCurrent()));
        b.append(" ");
        b.append(NSBundle.localizedString("of", "1.2MB of 3.4MB"));
        b.append(" ");
        b.append(Status.getSizeAsString(transfer.getSize()));
        if(transfer.isRunning() && null != meter) {
            float speed = meter.getSpeed();
            if(speed > -1) {
                b.append(" (");
                b.append(Status.getSizeAsString(speed));
                b.append("/sec, ");
                int remaining = (int) ((transfer.getSize() - meter.getBytesTransfered()) / speed);
                if(remaining > 120) {
                    b.append(remaining / 60);
                    b.append(" ");
                    b.append(MIN_REMAINING);
                }
                else {
                    b.append(remaining);
                    b.append(" ");
                    b.append(SEC_REMAINING);

                }
                b.append(")");
            }
        }
        if(this.statusText != null) {
            b.append(" - ");
            b.append(this.statusText);
        }
        return b.toString();
    }

    public Transfer getTransfer() {
        return this.transfer;
    }

    private boolean highlighted;

    public void setHighlighted(final boolean highlighted) {
        this.highlighted = highlighted;
        if(highlighted) {
            this.filenameField.setTextColor(NSColor.whiteColor());
            this.progressField.setTextColor(NSColor.whiteColor());
        }
        else {
            this.filenameField.setTextColor(NSColor.blackColor());
            this.progressField.setTextColor(NSColor.darkGrayColor());
        }
    }

    public boolean isHighlighted() {
        return this.highlighted;
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    private NSTextField filenameField; // IBOutlet

    public void setFilenameField(NSTextField filenameField) {
        this.filenameField = filenameField;
        this.filenameField.setEditable(false);
        this.filenameField.setSelectable(false);
        this.filenameField.setTextColor(NSColor.blackColor());
    }

    private NSTextField progressField; // IBOutlet

    public void setProgressField(final NSTextField progressField) {
        this.progressField = progressField;
        this.progressField.setEditable(false);
        this.progressField.setSelectable(false);
        this.progressField.setTextColor(NSColor.darkGrayColor());
    }

    private NSProgressIndicator progressBar; // IBOutlet

    public void setProgressBar(final NSProgressIndicator progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setDisplayedWhenStopped(false);
        this.progressBar.setControlTint(NSProgressIndicator.BlueControlTint);
        this.progressBar.setControlSize(NSProgressIndicator.SmallControlSize);
        this.progressBar.setStyle(NSProgressIndicator.ProgressIndicatorBarStyle);
        this.progressBar.setUsesThreadedAnimation(true);
    }

    private NSImageView statusIconView; //IBOutlet

    private static final NSImage RED_ICON = NSImage.imageNamed("statusRed.tiff");
    private static final NSImage GREEN_ICON = NSImage.imageNamed("statusGreen.tiff");
    private static final NSImage YELLOW_ICON = NSImage.imageNamed("statusYellow.tiff");

    public void setStatusIconView(final NSImageView statusIconView) {
        this.statusIconView = statusIconView;
        if(transfer.isQueued()) {
            this.statusIconView.setImage(YELLOW_ICON);
        }
        else {
            this.statusIconView.setImage(transfer.isComplete() ? GREEN_ICON : RED_ICON);
        }
    }

    private NSImageView typeIconView; //IBOutlet

    private static final NSImage ARROW_UP_ICON = NSImage.imageNamed("arrowUp.tiff");
    private static final NSImage ARROW_DOWN_ICON = NSImage.imageNamed("arrowDown.tiff");
    private static final NSImage SYNC_ICON = NSImage.imageNamed("sync32.tiff");

    static {
        ARROW_UP_ICON.setSize(new NSSize(32f, 32f));
        ARROW_DOWN_ICON.setSize(new NSSize(32f, 32f));
        SYNC_ICON.setSize(new NSSize(32f, 32f));
    }

    public void setTypeIconView(final NSImageView typeIconView) {
        this.typeIconView = typeIconView;
        NSImage icon = ARROW_DOWN_ICON;
        if(transfer instanceof UploadTransfer) {
            icon = ARROW_UP_ICON;
        }
        else if(transfer instanceof SyncTransfer) {
            icon = SYNC_ICON;
        }
        this.typeIconView.setImage(icon);
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