package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import com.apple.cocoa.application.NSTextField;
import com.apple.cocoa.application.NSView;
import com.apple.cocoa.foundation.NSAttributedString;
import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.ui.cocoa.threading.BackgroundException;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.jets3t.service.S3ServiceException;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import ch.ethz.ssh2.sftp.SFTPException;
import com.enterprisedt.net.ftp.FTPException;

/**
 * @version $Id:$
 */
public class CDErrorController extends CDBundleController {
    private static Logger log = Logger.getLogger(CDTaskController.class);

    private NSTextField hostField;

    public void setHostField(NSTextField hostField) {
        this.hostField = hostField;
        if(null == failure.getPath()) {
            this.hostField.setAttributedStringValue(
                    new NSAttributedString(failure.getSession().getHost().toURL(), TRUNCATE_MIDDLE_ATTRIBUTES));
        }
        else {
            this.hostField.setStringValue(failure.getPath().getAbsolute());
        }
    }

    private NSTextField descriptionField;

    public void setDescriptionField(NSTextField descriptionField) {
        this.descriptionField = descriptionField;
        this.descriptionField.setAttributedStringValue(
                new NSAttributedString(this.getDetailedCauseMessage(failure), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private NSTextField errorField;

    public void setErrorField(NSTextField errorField) {
        this.errorField = errorField;
        this.errorField.setAttributedStringValue(
                new NSAttributedString(this.getReadableTitle(failure) + ": " + failure.getMessage(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    private NSView view;

    public void setView(NSView view) {
        this.view = view;
    }

    public NSView view() {
        return view;
    }

    private BackgroundException failure;

    public CDErrorController(BackgroundException e) {
        this.failure = e;
        this.loadBundle();
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
            return ((SFTPException) cause).getServerErrorCodeVerbose();
        }
        if(cause instanceof S3ServiceException) {
            if(null != ((S3ServiceException) cause).getS3ErrorMessage()) {
                return cause.getMessage() + ". " + ((S3ServiceException) cause).getS3ErrorMessage();
            }
        }
        return NSBundle.localizedString(cause.getMessage(), "Error", "");
    }

    public void awakeFromNib() {
        ;
    }

    protected String getBundleName() {
        return "Error";
    }
}
