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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.ui.cocoa.application.NSTextField;
import ch.cyberduck.ui.cocoa.application.NSView;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.S3ServiceException;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import ch.ethz.ssh2.sftp.SFTPException;
import com.enterprisedt.net.ftp.FTPException;
import com.mosso.client.cloudfiles.FilesException;

/**
 * @version $Id:$
 */
public class CDErrorController extends CDBundleController {
    private static Logger log = Logger.getLogger(CDTaskController.class);

    @Outlet
    private NSTextField hostField;

    public void setHostField(NSTextField hostField) {
        this.hostField = hostField;
        if(null == failure.getPath()) {
            this.hostField.setAttributedStringValue(
                    NSAttributedString.attributedStringWithAttributes(failure.getSession().getHost().toURL(), FIXED_WITH_FONT_ATTRIBUTES));
        }
        else {
            this.hostField.setAttributedStringValue(
                    NSAttributedString.attributedStringWithAttributes(failure.getPath().getAbsolute(), FIXED_WITH_FONT_ATTRIBUTES));
        }
    }

    @Outlet
    private NSTextField descriptionField;

    public void setDescriptionField(NSTextField descriptionField) {
        this.descriptionField = descriptionField;
        this.descriptionField.setSelectable(true);
        this.descriptionField.setAttributedStringValue(
                NSAttributedString.attributedStringWithAttributes(this.getDetailedCauseMessage(failure), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    @Outlet
    private NSTextField errorField;

    public void setErrorField(NSTextField errorField) {
        this.errorField = errorField;
        this.errorField.setSelectable(true);
        this.errorField.setAttributedStringValue(
                NSAttributedString.attributedStringWithAttributes(this.getReadableTitle(failure) + ": " + failure.getMessage(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    @Outlet
    private NSView view;

    public void setView(NSView view) {
        this.view = view;
    }

    @Override
    public NSView view() {
        return view;
    }

    private BackgroundException failure;

    public CDErrorController(BackgroundException e) {
        this.failure = e;
        this.loadBundle();
    }

    /**
     * @return
     */
    public String getTooltip() {
        return this.getReadableTitle(failure);
    }

    private String getReadableTitle(BackgroundException e) {
        final Throwable cause = e.getCause();
        if(cause instanceof FTPException) {
            return "FTP " + Locale.localizedString("Error");
        }
        if(cause instanceof SFTPException) {
            return "SSH " + Locale.localizedString("Error");
        }
        if(cause instanceof S3ServiceException) {
            return "S3 " + Locale.localizedString("Error");
        }
        if(cause instanceof CloudFrontServiceException) {
            return "CloudFront " + Locale.localizedString("Error");
        }
        if(cause instanceof HttpException) {
            return "HTTP " + Locale.localizedString("Error");
        }
        if(cause instanceof SocketException) {
            return "Network " + Locale.localizedString("Error");
        }
        if(cause instanceof UnknownHostException) {
            return "DNS " + Locale.localizedString("Error");
        }
        if(cause instanceof IOException) {
            return "I/O " + Locale.localizedString("Error");
        }
        return Locale.localizedString("Error");
    }

    private String getDetailedCauseMessage(BackgroundException e) {
        final Throwable cause = e.getCause();
        StringBuilder buffer = new StringBuilder();
        if(null != cause) {
            if(StringUtils.isNotBlank(cause.getMessage())) {
                buffer.append(cause.getMessage());
            }
            if(cause instanceof SFTPException) {
                ;
            }
            if(cause instanceof S3ServiceException) {
                final S3ServiceException s3 = (S3ServiceException) cause;
                if(StringUtils.isNotBlank(s3.getResponseStatus())) {
                    // HTTP method status
                    buffer.append(" ").append(s3.getResponseStatus());
                }
                if(StringUtils.isNotBlank(s3.getS3ErrorMessage())) {
                    // S3 protocol message
                    buffer.append(" ").append(s3.getS3ErrorMessage());
                }
            }
            if(cause instanceof CloudFrontServiceException) {
                final CloudFrontServiceException cf = (CloudFrontServiceException) cause;
                if(StringUtils.isNotBlank(cf.getErrorMessage())) {
                    buffer.append(cf.getErrorMessage()).append(". ");
                }
                if(StringUtils.isNotBlank(cf.getErrorDetail())) {
                    buffer.append(" ").append(cf.getErrorDetail());
                }
            }
            if(cause instanceof FilesException) {
                final FilesException cf = (FilesException) cause;
                final StatusLine status = cf.getHttpStatusLine();
                if(null != status) {
                    if(StringUtils.isNotBlank(status.getReasonPhrase())) {
                        buffer.append(" ").append(status.getReasonPhrase());
                    }
                }
            }
        }
        String message = buffer.toString();
        if(!message.endsWith(".")) {
            message = message + ".";
        }
        return Locale.localizedString(message, "Error");
    }

    @Override
    protected String getBundleName() {
        return "Error";
    }
}
