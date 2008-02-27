package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
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

import ch.ethz.ssh2.sftp.SFTPException;
import com.enterprisedt.net.ftp.FTPException;

import ch.cyberduck.ui.cocoa.threading.BackgroundException;

import com.apple.cocoa.application.NSGraphics;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSRect;

import org.jets3t.service.S3ServiceException;
import org.apache.commons.httpclient.HttpException;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @version $Id$
 */
public class CDErrorCell extends CDTableCell {

    private BackgroundException failure;

    public CDErrorCell() {
        super();
    }

    public void setObjectValue(final Object exception) {
        this.failure = (BackgroundException) exception;
    }

    private String getReadableTitle(BackgroundException e) {
        String title = NSBundle.localizedString("Error", "");
        final Throwable cause = e.getCause();
        if(cause instanceof FTPException) {
            title = "FTP " + NSBundle.localizedString("Error", "");
        }
        else if(cause instanceof SFTPException) {
            title = "SSH " + NSBundle.localizedString("Error", "");
        }
        else if(cause instanceof S3ServiceException) {
            title = "S3 " + NSBundle.localizedString("Error", "");
        }
        else if(cause instanceof HttpException) {
            title = "HTTP " + NSBundle.localizedString("Error", "");
        }
        else if(cause instanceof SocketException) {
            title = "Network " + NSBundle.localizedString("Error", "");
        }
        else if(cause instanceof UnknownHostException) {
            title = "DNS " + NSBundle.localizedString("Error", "");
        }
        else if(cause instanceof IOException) {
            title = "I/O " + NSBundle.localizedString("Error", "");
        }
        return title;
    }

    private String getDetailedCauseMessage(BackgroundException e) {
        final Throwable cause = e.getCause();
        if(cause instanceof SFTPException) {
            return ((SFTPException)cause).getServerErrorCodeVerbose();
        }
        if(cause instanceof S3ServiceException) {
            if(null != ((S3ServiceException)cause).getS3ErrorMessage()) {
                return cause.getMessage()+". "+((S3ServiceException)cause).getS3ErrorMessage();
            }
        }
        return NSBundle.localizedString(cause.getMessage(), "Error", "");
    }

    public void drawInteriorWithFrameInView(NSRect cellFrame, NSView controlView) {
        super.drawInteriorWithFrameInView(cellFrame, controlView);
        if (failure != null) {
            NSGraphics.drawAttributedString(new NSAttributedString(this.getReadableTitle(failure)
                    +": "+failure.getMessage(),
                    boldFont),
                    new NSRect(cellFrame.origin().x() + 5, cellFrame.origin().y() + 1,
                            cellFrame.size().width() - 5, cellFrame.size().height()));
            if(null == failure.getPath()) {
                NSGraphics.drawAttributedString(new NSAttributedString(failure.getSession().getHost().toURL(),
                        fixedFont),
                        new NSRect(cellFrame.origin().x() + 5, cellFrame.origin().y() + 16,
                                cellFrame.size().width() - 5, cellFrame.size().height()));
            }
            else {
                NSGraphics.drawAttributedString(new NSAttributedString(failure.getPath().getAbsolute(),
                        fixedFont),
                        new NSRect(cellFrame.origin().x() + 5, cellFrame.origin().y() + 16,
                                cellFrame.size().width() - 5, cellFrame.size().height()));
            }
            NSGraphics.drawAttributedString(new NSAttributedString(this.getDetailedCauseMessage(failure),
                    alertFont),
                    new NSRect(cellFrame.origin().x() + 5, cellFrame.origin().y() + 33,
                            cellFrame.size().width() - 5, cellFrame.size().height()));
        }
    }
}