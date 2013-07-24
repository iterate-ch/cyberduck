package ch.cyberduck.core.exception;

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

import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * @version $Id$
 */
public class BackgroundException extends Exception {
    private static final long serialVersionUID = -6114495291207129418L;

    private String message = Locale.localizedString("Unknown");
    private String detail;

    public BackgroundException() {
        this(Locale.localizedString("Unknown"), null);
    }

    public BackgroundException(final Exception cause) {
        this(cause.getMessage(), cause);
    }

    public BackgroundException(final String detail) {
        this(detail, null);
    }

    public BackgroundException(final String detail, final Exception cause) {
        super(cause);
        this.detail = detail;
    }

    public void setMessage(final String title) {
        this.message = title;
    }

    @Override
    public String getMessage() {
        return Locale.localizedString(message, "Error");
    }

    /**
     * @return Detailed message from the underlying cause.
     */
    public String getDetail() {
        return detail;
    }

    /**
     * @return What kind of error
     */
    public String getTitle() {
        final Throwable cause = this.getCause();
        if(cause instanceof SocketException) {
            return String.format("Network %s", Locale.localizedString("Error"));
        }
        if(cause instanceof UnknownHostException) {
            return String.format("DNS %s", Locale.localizedString("Error"));
        }
        if(cause instanceof IOException) {
            return String.format("I/O %s", Locale.localizedString("Error"));
        }
        return Locale.localizedString("Error");
    }

    public boolean isNetworkFailure() {
        final Throwable cause = this.getCause();
        if(cause instanceof SSLException) {
            return StringUtils.contains(cause.getMessage(), "Received close_notify during handshake");
        }
        return cause instanceof SocketException
                || cause instanceof SocketTimeoutException
                || cause instanceof UnknownHostException;
    }

    @Override
    public String toString() {
        return String.format("%s. %s", message, detail);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof BackgroundException)) {
            return false;
        }
        BackgroundException that = (BackgroundException) o;
        if(this.getCause() != null ? !this.getCause().equals(that.getCause()) : that.getCause() != null) {
            return false;
        }
        if(detail != null ? !detail.equals(that.detail) : that.detail != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = detail != null ? detail.hashCode() : 0;
        result = 31 * result + (this.getCause() != null ? this.getCause().hashCode() : 0);
        return result;
    }
}
