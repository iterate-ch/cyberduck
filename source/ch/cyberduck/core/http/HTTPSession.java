package ch.cyberduck.core.http;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
* Opens a connection to the remote server via http protocol
 * @version $Id$
 */
public class HTTPSession extends Session {
    private static Logger log = Logger.getLogger(Session.class);

    protected HttpClient HTTP;

    public HTTPSession(Host h) {
        super(h);
        this.HTTP = new HttpClient();
    }

    public void close() {
	this.callObservers(new Message(Message.CLOSE, "Closing session."));
	try {
	    HTTP.quit();
	}
	catch(IOException e) {
	    log.error(e.getMessage());
	}
	finally {
	    this.setConnected(false);
	}
    }

    public void check() throws IOException {
	log.debug("check");
	if(!HTTP.isAlive()) {
	  //  host.recycle();
	    this.setConnected(false);
	    this.connect();
	    while(true) {
		if(this.isConnected())
		    return;
		this.log("Waiting for connection...", Message.PROGRESS);
		Thread.yield();
	    }
	}
    }


    public Session copy() {
	return new HTTPSession(this.host);
    }
    

    public synchronized void connect() {
//		host.status.fireActiveEvent();
	this.callObservers(new Message(Message.OPEN, "Opening session."));
	HTTPSession.this.log("Opening HTTP connection to " + host.getIp() +"...", Message.PROGRESS);
//		if(Preferences.instance().getProperty("connection.proxy").equals("true")) {
//		    HTTP.connect(host.getName(), host.getPort(), Preferences.instance().getProperty("connection.proxy.host"), Integer.parseInt(Preferences.instance().getProperty("connection.proxy.port")));
//		}
//		else {
	HTTP.connect(host.getName(), host.getPort(), false);//@todo implement https
//		}
	    this.setConnected(true);
	    log("HTTP connection opened", Message.PROGRESS);
//		HTTPFile p = new HTTPFile(host.getWorkdir());
//		p.download();
//		host.status.fireStopEvent();
    }
    
    public void mount() {
	this.log("Invalid Operation", Message.ERROR);
    }

    public Path workdir() {
	this.log("Invalid Operation", Message.ERROR);
	return null;
    }
}