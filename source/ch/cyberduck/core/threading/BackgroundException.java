package ch.cyberduck.core.threading;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.ftp.FTPException;
import ch.cyberduck.core.i18n.Locale;
import ch.ethz.ssh2.sftp.SFTPException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.StatusLine;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.ServiceException;
import org.soyatec.windows.azure.error.StorageServerException;

import com.rackspacecloud.client.cloudfiles.FilesException;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class BackgroundException extends Exception {

    private String message;

    private Path path;

    private Session session;

    public BackgroundException(Session session, Path path, String message, Throwable cause) {
        super(cause);
        this.session = session;
        this.path = path;
        if(null == path) {
            this.message = StringUtils.chomp(message);
        }
        else {
            this.message = MessageFormat.format(StringUtils.chomp(message), path.getName());
        }
    }

    @Override
    public String getMessage() {
        return Locale.localizedString(message, "Error");
    }

    /**
     * @return The real cause of the exception thrown
     */
    @Override
    public Throwable getCause() {
        Throwable cause = super.getCause();
        if(null == cause) {
            return this;
        }
        while(cause.getCause() != null && StringUtils.isNotBlank(cause.getCause().getMessage())) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * @return What kind of error
     */
    public String getReadableTitle() {
        final Throwable cause = this.getCause();
        if(cause instanceof FTPException) {
            return "FTP " + Locale.localizedString("Error");
        }
        if(cause instanceof SFTPException) {
            return "SSH " + Locale.localizedString("Error");
        }
        if(cause instanceof ServiceException) {
            return "S3 " + Locale.localizedString("Error");
        }
        if(cause instanceof CloudFrontServiceException) {
            return "CloudFront " + Locale.localizedString("Error");
        }
        if(cause instanceof org.apache.commons.httpclient.HttpException) {
            return "HTTP " + Locale.localizedString("Error");
        }
        if(cause instanceof org.apache.http.HttpException) {
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

    /**
     * @return Detailed message from the underlying cause.
     */
    public String getDetailedCauseMessage() {
        final Throwable cause = this.getCause();
        StringBuilder buffer = new StringBuilder();
        if(null != cause) {
            if(StringUtils.isNotBlank(cause.getMessage())) {
                String m = StringUtils.chomp(cause.getMessage());
                buffer.append(m);
                if(!m.endsWith(".")) {
                    buffer.append(".");
                }
            }
            if(cause instanceof SFTPException) {
                ;
            }
            if(cause instanceof ServiceException) {
                final ServiceException s3 = (ServiceException) cause;
                if(StringUtils.isNotBlank(s3.getResponseStatus())) {
                    // HTTP method status
                    buffer.append(" ").append(s3.getResponseStatus()).append(".");
                }
                if(StringUtils.isNotBlank(s3.getErrorMessage())) {
                    // S3 protocol message
                    buffer.append(" ").append(s3.getErrorMessage());
                }
            }
            if(cause instanceof org.jets3t.service.impl.rest.HttpException) {
                final org.jets3t.service.impl.rest.HttpException http = (org.jets3t.service.impl.rest.HttpException) cause;
                buffer.append(" ").append(http.getResponseCode());
                if(StringUtils.isNotBlank(http.getResponseMessage())) {
                    buffer.append(" ").append(http.getResponseMessage());
                }
            }
            if(cause instanceof CloudFrontServiceException) {
                final CloudFrontServiceException cf = (CloudFrontServiceException) cause;
                if(StringUtils.isNotBlank(cf.getErrorMessage())) {
                    buffer.append(" ").append(cf.getErrorMessage());
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
            if(cause instanceof StorageServerException) {
                buffer.delete(buffer.indexOf("\r\n"), buffer.length());
            }
        }
        String message = buffer.toString();
        if(!StringUtils.isEmpty(message)) {
            if(Character.isLetter(message.charAt(message.length() - 1))) {
                message = message + ".";
            }
        }
        return Locale.localizedString(message, "Error");
    }

    /**
     * @return The path accessed when the exception was thrown or null if
     *         the exception is not related to any path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @return The session this exception occured
     */
    public Session getSession() {
        return session;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BackgroundException) {
            BackgroundException other = (BackgroundException) obj;
            if(!this.getSession().equals(other.getSession())) {
                return false;
            }
            if(null == this.getPath() || null == other.getPath()) {
                return this.getCause().getMessage().equals(other.getCause().getMessage());
            }
            if(!this.getPath().equals(other.getPath())) {
                return false;
            }
            return this.getCause().getMessage().equals(other.getCause().getMessage());
        }
        return super.equals(obj);
    }
}
