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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @version $Id$
 */
public class BackgroundException extends Exception {
    private static Logger log = Logger.getLogger(BackgroundException.class);

    private static final long serialVersionUID = -6114495291207129418L;

    private String title = Locale.localizedString("Unknown");
    private String detail;

    private Path path;

    private Host host;

    public BackgroundException(final Exception cause) {
        this(null, null, cause.getMessage(), cause);
    }

    public BackgroundException(final String detail, final Exception cause) {
        this(null, null, detail, cause);
    }

    public BackgroundException(final Host host, final Exception cause) {
        this(host, null, Locale.localizedString("Unknown"), cause);
    }

    public BackgroundException(final Host host, final String detail, final Exception cause) {
        this(host, null, detail, cause);
    }

    public BackgroundException(final Path path, final String detail, final Exception cause) {
        this(null, path, detail, cause);
    }

    public BackgroundException(final Host host, final Path path, final String detail, final Exception cause) {
        super(cause);
        this.host = host;
        this.path = path;
        this.detail = detail;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setPath(final Path path) {
        this.path = path;
    }

    public void setHost(final Host host) {
        this.host = host;
    }

    @Override
    public String getMessage() {
        return Locale.localizedString(title, "Error");
    }

    /**
     * @return Detailed message from the underlying cause.
     */
    public String getDetail() {
        return detail;
    }

    @Deprecated
    public String getDetailedCauseMessage() {
        return this.getDetail();
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

    @Deprecated
    public String getReadableTitle() {
        return this.getTitle();
    }

    /**
     * @return The path accessed when the exception was thrown or null if
     *         the exception is not related to any path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @return The session this exception occurred
     */
    public Host getHost() {
        return host;
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
        if(path != null ? !path.equals(that.path) : that.path != null) {
            return false;
        }
        if(host != null ? !host.equals(that.host) : that.host != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = detail != null ? detail.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (this.getCause() != null ? this.getCause().hashCode() : 0);
        return result;
    }
}
