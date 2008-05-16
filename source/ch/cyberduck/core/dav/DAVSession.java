package ch.cyberduck.core.dav;

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

import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.core.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * @version $Id: $
 */
public class DAVSession extends Session {
    private static Logger log = Logger.getLogger(DAVSession.class);

    static {
        SessionFactory.addFactory(Protocol.WEBDAV, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new DAVSession(h);
        }
    }

    protected DAVResource DAV;

    protected DAVSession(Host h) {
        super(h);
    }

    private final String ua = NSBundle.mainBundle().objectForInfoDictionaryKey("CFBundleName") + "/"
            + Preferences.instance().getProperty("version");

    protected void configure() throws IOException {
        final HttpClient client = this.DAV.getSessionInstance(this.DAV.getHttpURL(), false);
        client.getHostConfiguration().getParams().setParameter(
                "http.useragent", ua
        );
        if(Proxy.isHTTPProxyEnabled()) {
            this.DAV.setProxy(Proxy.getHTTPProxyHost(), Proxy.getHTTPProxyPort());
        }
        this.DAV.setFollowRedirects(Preferences.instance().getBoolean("webdav.followRedirects"));
    }

    protected void connect() throws IOException, LoginCanceledException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        this.message(MessageFormat.format(NSBundle.localizedString("Opening {0} connection to {1}", "Status", ""),
                new Object[]{host.getProtocol().getName(), host.getHostname()}));

        WebdavResource.setDefaultAction(WebdavResource.NOACTION);

        this.DAV = new DAVResource(host.toURL());
        final String workdir = host.getDefaultPath();
        if(StringUtils.hasText(workdir)) {
            this.DAV.setPath(workdir.startsWith(Path.DELIMITER) ? workdir : Path.DELIMITER + workdir);
        }

        this.configure();
        this.login();

        WebdavResource.setDefaultAction(WebdavResource.BASIC);

        this.message(MessageFormat.format(NSBundle.localizedString("{0} connection opened", "Status", ""),
                new Object[]{host.getProtocol().getName()}));

        if(null == this.DAV.getResourceType() || !this.DAV.getResourceType().isCollection()) {
            throw new IOException("Listing directory failed");
        }

        this.fireConnectionDidOpenEvent();
    }

    protected void login() throws IOException, LoginCanceledException {
        final Credentials credentials = host.getCredentials();
        login.check(credentials, host.getProtocol(), host.getHostname());
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        try {
            this.message(MessageFormat.format(NSBundle.localizedString("Authenticating as {0}", "Status", ""),
                    new Object[]{credentials.getUsername()}));

            if(!credentials.isAnonymousLogin()) {
                this.DAV.setCredentials(
                        new UsernamePasswordCredentials(credentials.getUsername(),
                                credentials.getPassword())
                );
                this.DAV.setUserInfo(credentials.getUsername(),
                        credentials.getPassword());
            }
            this.configure();

            // Try to get basic properties fo this resource using these credentials
            this.DAV.setProperties(WebdavResource.BASIC, DepthSupport.DEPTH_0);

            this.message(NSBundle.localizedString("Login successful", "Credentials", ""));
        }
        catch(HttpException e) {
            if(e.getReasonCode() == HttpStatus.SC_UNAUTHORIZED) {
                this.message(NSBundle.localizedString("Login failed", "Credentials", ""));
                this.login.fail(host.getProtocol(), credentials,
                        e.getReason());
                this.login();
            }
            else {
                throw e;
            }
        }
    }

    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
                DAV.close();
            }
        }
        catch(IOException e) {
            log.error("IO Error: " + e.getMessage());
        }
        finally {
            DAV = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    public void interrupt() {
        try {
            super.interrupt();
            this.fireConnectionWillCloseEvent();
            if(this.isConnected()) {
                DAV.close();
            }
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        finally {
            DAV = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    public Path workdir() throws ConnectionCanceledException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        if(null == workdir) {
            workdir = PathFactory.createPath(this, DAV.getPath(), Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
        return workdir;
    }

    public void setWorkdir(Path workdir) throws IOException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        DAV.setPath(workdir.isRoot() ? Path.DELIMITER : workdir.getAbsolute() + Path.DELIMITER);
    }

    protected void noop() throws IOException {
        if(this.isConnected()) {
            DAV.getStatusMessage();
        }
    }

    public void sendCommand(String command) {
        throw new UnsupportedOperationException();
    }

    public boolean isConnected() {
        return DAV != null;
    }

    public void error(Path path, String message, Throwable e) {
        if(e instanceof HttpException) {
            super.error(path, message, new HttpException(
                    HttpStatus.getStatusText(((HttpException) e).getReasonCode())));
        }
        super.error(path, message, e);
    }
}