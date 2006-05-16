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

import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.QueueListener;
import ch.cyberduck.core.Status;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDProgressController extends CDController {
    private static Logger log = Logger.getLogger(CDProgressController.class);

    private String statusText;
    private StringBuffer errorText;

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

    private Queue queue;

    private static final Object lock = new Object();

    public CDProgressController(Queue queue) {
        this.queue = queue;
        synchronized(lock) {
            if(!NSApplication.loadNibNamed("Progress", this)) {
                log.fatal("Couldn't load Progress.nib");
            }
        }
    }

    public void init() {
        this.queue.addListener(new QueueListener() {
            private ProgressListener progress;

            public void queueStarted() {
                invoke(new Runnable() {
                    public void run() {
                        progressBar.setHidden(false);
                        progressBar.setIndeterminate(true);
                        progressBar.startAnimation(null);
                        progressBar.setNeedsDisplay(true);
                        errorText = new StringBuffer();
                        alertIcon.setHidden(true);
                    }
                });
                queue.getSession().addProgressListener(progress = new ProgressListener() {
                    public void message(final String message) {
                        statusText = message;
                        invoke(new Runnable() {
                            public void run() {
                                updateProgressfield();
                            }
                        });
                    }

                    public void error(final Exception e) {
                        invoke(new Runnable() {
                            public void run() {
                                int l = errorText.toString().split("\n").length;
                                if(l == 10) {
                                    errorText.append("\n- (...)");
                                }
                                if(l < 10) {
                                    errorText.append("\n" + e.getMessage());
                                }
                                alertIcon.setHidden(false);
                            }
                        });
                    }
                });
            }

            public void queueStopped() {
                invoke(new Runnable() {
                    public void run() {
                        updateProgressfield();
                        progressBar.setIndeterminate(true);
                        progressBar.stopAnimation(null);
                        progressBar.setHidden(true);
                    }
                });
                queue.removeListener(this);
                queue.getSession().removeProgressListener(progress);
            }

            public void transferStarted() {
                meter = new Speedometer();
                progressTimer = new NSTimer(0.1, //seconds
                        CDProgressController.this, //target
                        new NSSelector("update", new Class[]{NSTimer.class}),
                        queue, //userInfo
                        true); //repeating
                mainRunLoop.addTimerForMode(progressTimer,
                        NSRunLoop.DefaultRunLoopMode);
            }

            public void transferStopped() {
                progressTimer.invalidate();
            }
        });
    }

    public void awakeFromNib() {
        this.filenameField.setAttributedStringValue(new NSAttributedString(this.queue.getName(),
                TRUNCATE_TAIL_PARAGRAPH_DICTIONARY));
        this.updateProgressfield();
    }

    public void update(NSTimer t) {
        this.updateProgressbar();
        this.updateProgressfield();
    }

    private Speedometer meter;

    private class Speedometer {
        //the time to start counting bytes transfered
        private long timestamp = System.currentTimeMillis();
        //initial data already transfered
        private final double initialBytesTransfered = queue.getCurrent();
        private double bytesTransferred = 0;

        /**
         * Returns the data transfer rate. The rate should depend on the transfer
         * rate timestamp.
         *
         * @return The bytes being processed per second
         */
        public float getSpeed() {
            bytesTransferred = queue.getCurrent();
            if(bytesTransferred > initialBytesTransfered) {
                // number of seconds data was actually transferred
                double elapsedTime = (System.currentTimeMillis() - timestamp) / 1000;
                if(elapsedTime > 1) {
                    // bytes per second
                    return (float) ((bytesTransferred-initialBytesTransfered) / (elapsedTime));
                }
            }
            return -1;
        }

        public double getBytesTransfered() {
            return bytesTransferred;
        }
    }

    private void updateProgressbar() {
        if(queue.isInitialized()) {
            if(queue.getSize() != -1) {
                this.progressBar.setIndeterminate(false);
                this.progressBar.setMinValue(0);
                this.progressBar.setMaxValue(queue.getSize());
                this.progressBar.setDoubleValue(queue.getCurrent());
            }
        }
        else if(queue.isRunning()) {
            this.progressBar.setIndeterminate(true);
        }
    }

    private void updateProgressfield() {
        this.progressField.setAttributedStringValue(new NSAttributedString(this.getProgressText(),
                TRUNCATE_MIDDLE_PARAGRAPH_DICTIONARY));
    }

    private static final String SEC_REMAINING = NSBundle.localizedString("seconds remaining", "Status", "");
    private static final String MIN_REMAINING = NSBundle.localizedString("minutes remaining", "Status", "");

    private String getProgressText() {
        StringBuffer b = new StringBuffer();
        b.append(Status.getSizeAsString(this.queue.getCurrent()));
        b.append(" ");
        b.append(NSBundle.localizedString("of", "1.2MB of 3.4MB"));
        b.append(" ");
        b.append(Status.getSizeAsString(this.queue.getSize()));
        if(queue.isRunning() && null != meter) {
            float speed = meter.getSpeed();
            if(speed > -1) {
                b.append(" (");b.append(Status.getSizeAsString(speed));b.append("/sec, ");
                int remaining = (int)((queue.getSize()-meter.getBytesTransfered())/speed);
                if(remaining > 120) {
                    b.append(remaining/60);b.append(" ");b.append(MIN_REMAINING);
                }
                else {
                    b.append(remaining);b.append(" ");b.append(SEC_REMAINING);

                }
                b.append(")");
            }
            b.append(" ");
            b.append(this.statusText);
        }
        return b.toString();
    }

    private String getErrorText() {
        return this.errorText.toString();
    }

    public Queue getQueue() {
        return this.queue;
    }

    public void alertButtonClicked(final Object sender) {
        CDQueueController.instance().alert(
                NSAlertPanel.criticalAlertPanel(NSBundle.localizedString("Error", "Alert sheet title"),
                        this.getErrorText(), // message
                        NSBundle.localizedString("OK", "Alert default button"), // defaultbutton
                        null, //alternative button
                        null //other button
                ));
    }

    private boolean highlighted;

    public void setHighlighted(boolean highlighted) {
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

    public void setProgressField(NSTextField progressField) {
        this.progressField = progressField;
        this.progressField.setEditable(false);
        this.progressField.setSelectable(false);
        this.progressField.setTextColor(NSColor.darkGrayColor());
    }

    private NSProgressIndicator progressBar; // IBOutlet

    public void setProgressBar(NSProgressIndicator progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setDisplayedWhenStopped(false);
        this.progressBar.setControlTint(NSProgressIndicator.BlueControlTint);
        this.progressBar.setControlSize(NSProgressIndicator.SmallControlSize);
        this.progressBar.setStyle(NSProgressIndicator.ProgressIndicatorBarStyle);
        this.progressBar.setUsesThreadedAnimation(true);
    }

    private NSButton alertIcon; // IBOutlet

    public void setAlertIcon(NSButton alertIcon) {
        this.alertIcon = alertIcon;
        this.alertIcon.setHidden(true);
        this.alertIcon.setTarget(this);
        this.alertIcon.setAction(new NSSelector("alertButtonClicked", new Class[]{Object.class}));
    }

    private NSView progressView; // IBOutlet

    public void setProgressSubview(NSView progressView) {
        this.progressView = progressView;
    }

    public NSView view() {
        return this.progressView;
    }
}