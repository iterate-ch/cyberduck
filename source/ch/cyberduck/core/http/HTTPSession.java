package ch.cyberduck.core.http;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
 * Opens a connection to the remote server via http protocol
 *
 * @version $Id$
 */
public class HTTPSession extends Session {
    private static Logger log = Logger.getLogger(Session.class);

    static {
        SessionFactory.addFactory(Session.HTTP, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new HTTPSession(h);
        }
    }

    protected HttpClient HTTP;
    //    protected HttpConnection HTTP;

    public HTTPSession(Host h) {
        super(h);
        //        this.HTTP = new HttpConnection(h.getHostname(), h.getPort());
    }

    public synchronized void close() {
		this.callObservers(new Message(Message.CLOSE, "Closing session."));
        try {
            if (this.HTTP != null) {
                this.log("Disconnecting...", Message.PROGRESS);
                this.HTTP.quit();
                this.getHost().getLogin().setPassword(null);
                this.HTTP = null;
            }
        }
        catch (IOException e) {
            this.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            this.log("Disconnected", Message.PROGRESS);
            this.setConnected(false);
        }
    }

    public synchronized void connect() throws IOException {
        this.callObservers(new Message(Message.OPEN, "Opening session."));
        this.log("Opening HTTP connection to " + host.getIp() + "...", Message.PROGRESS);
        this.log(new java.util.Date().toString(), Message.TRANSCRIPT);
        this.log(host.getIp(), Message.TRANSCRIPT);
        this.HTTP = new HttpClient();
        this.HTTP.connect(host.getHostname(), host.getPort(), false);
        this.setConnected(true);
        this.log("HTTP connection opened", Message.PROGRESS);
    }

    public synchronized void mount() {
        this.log("Invalid Operation", Message.ERROR);
    }

    public void check() throws IOException {
        log.debug(this.toString() + ":check");
        this.log("Working", Message.START);
//		this.log("Checking connection...", Message.PROGRESS);
        if (null == HTTP || !HTTP.isAlive()) {
            this.setConnected(false);
            this.connect();
        }
    }

    public Path workdir() {
        this.log("Invalid Operation", Message.ERROR);
        return null;
    }
}