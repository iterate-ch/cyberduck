package ch.cyberduck.core.http;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Session;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
* @version $Id$
 */
public class HTTPPath extends Path {
    private static Logger log = Logger.getLogger(HTTPPath.class);

    private HTTPSession session;

    public HTTPPath(HTTPSession session, String parent, String name) {
	super(parent, name);
	this.session = session;
    }

    public HTTPPath(HTTPSession session, String path) {
	super(path);
	this.session = session;
    }

    public Path getParent() {
	String abs = this.getAbsolute();
	if((null == parent)) {
	    int index = abs.lastIndexOf('/');
	    String dirname = abs;
	    if(index > 0)
		dirname = abs.substring(0, index);
	    if(index == 0) //parent is root
		dirname = "/";
	    parent = new HTTPPath(session, dirname);
	}
	log.debug("getParent:"+parent);
	return parent;
    }

    public Session getSession() {
	return this.session;
    }
    
    public List list(boolean notifyobservers, boolean showHidden) {
	session.log("Invalid Operation", Message.ERROR);
	return null;
    }

    public List list() {
	session.log("Invalid Operation", Message.ERROR);
	return null;
    }

    public void delete() {
	session.log("Invalid Operation", Message.ERROR);
    }
    public void rename(String filename) {
	session.log("Invalid Operation", Message.ERROR);
    }
    public Path mkdir(String name) {
	session.log("Invalid Operation", Message.ERROR);
	return null;
    }

    public void changePermissions(int p) {
	session.log("Invalid Operation", Message.ERROR);
    }

    public void changeOwner(String owner) {
	session.log("Invalid Operation", Message.ERROR);
    }

    public void changeGroup(String group) {
	session.log("Invalid Operation", Message.ERROR);
    }

    public void fillQueue(Queue queue, Session session, int kind) {
//	    Queue queue = new Queue(this, kind);
//	List queue = new ArrayList();
	try {
	    this.session = (HTTPSession)session;
//	    this.session = (HTTPSession)this.getSession().copy();
	    this.session.check();
	    switch(kind) {
		case Queue.KIND_DOWNLOAD:
		    this.fillDownloadQueue(queue);
		    break;
		default:
		    throw new IllegalArgumentException("Upload not supported");
	    }
	}
	catch(IOException e) {
	    session.log(e.getMessage(), Message.ERROR);
	}
    }

    private void fillDownloadQueue(Queue queue) {
	queue.add(this);
    }

    public void download() {
	GetMethod GET = null;
	try {
	    log.debug("download:"+this.toString());
//	    this.status.fireActiveEvent();
	    session.check();
	    GET = new GetMethod(this.getAbsolute()); //@todo encode url
//	    GET.setUseDisk(false);
//	@todo proxy		if(Preferences.instance().getProperty("connection.proxy.authenticate").equals("true")) {
		    // enter the username and password for the proxy
//			    String authString = Preferences.instance().getProperty("connection.proxy.username")+":"+Preferences.instance().getProperty("connection.proxy.password");
		    // base64 encode the password.
//			    String auth = "Basic " + Base64.encode(authString.getBytes());
		    // Set up the connection so it knows we are sending proxy user information
//			    GET.addRequestHeader( "Proxy-Authorization", auth );
//			}

	    GET.setFollowRedirects(true);	    
//	    GET.addRequestHeader("Accept", GET.getAcceptHeader());
	    GET.addRequestHeader("User-Agent", "Cyberduck/" + Preferences.instance().getProperty("version"));
	    if(this.status.isResume()) {
		GET.addRequestHeader("Range", "bytes=" + this.status.getCurrent() + "-");
	    }
	    String v = GET.isHttp11() ? "session.HTTP/1.1" : "session.HTTP/1.0";
	    session.log("GET " + this.getAbsolute() + " " + v, Message.TRANSCRIPT);
	    Header[] requestHeaders = GET.getRequestHeaders();
	    for(int i = 0; i < requestHeaders.length; i++) {
		session.log(requestHeaders[i].toExternalForm(), Message.TRANSCRIPT);
	    }

	    int response = -1;
	    int attempts = 0;
	    while(response == -1 && attempts < 3) {
		try {
		    response = GET.execute(new HttpState(), session.HTTP) ;
//		    response = session.HTTP.executeMethod(GET);
		    session.log(response + " " + GET.getStatusText(), Message.TRANSCRIPT);
		    Header[] responseHeaders = GET.getResponseHeaders();
		    for(int i = 0; i < responseHeaders.length; i++) {
			session.log(responseHeaders[i].toString(), Message.TRANSCRIPT);
//			session.log(responseHeaders[i].toExternalForm(), Message.TRANSCRIPT);
		    }
		    attempts++;
		}
		catch(HttpRecoverableException e) {
		    session.log(e.getMessage(), Message.PROGRESS);
		}
	    }
	    if(response == -1) {
		throw new HttpException("Failed to recover from exception");
	    }

	    if(GET.getStatusCode() != HttpStatus.SC_OK) {
		throw new HttpException(GET.getStatusText());
	    }
//	    if(!HttpStatus.isSuccessfulResponse(response)) {
//		throw new HttpException(HttpStatus.getStatusText(response), response);
//	    }

	    if(this.status.isResume()) {
		if(GET.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
		    session.log("Resumption not possible.", Message.PROGRESS);
		    //session.log("Resumption not possible.", Message.ERROR);
		    this.status.setCurrent(0);
		    this.status.setResume(false);
		}
		else {
		    session.log("Resume at " + this.status.getCurrent() + ".", Message.PROGRESS);
		}
	    }

	    Header lengthHeader = GET.getResponseHeader("Content-Length");
	    if(lengthHeader != null) {
		try {
		    this.status.setSize(Integer.parseInt(lengthHeader.getValue()));
		}
		catch(NumberFormatException e) {
		    this.status.setSize(-1);
		}
	    }
	    Header rangeHeader = GET.getResponseHeader("Content-Range"); //Content-Range: bytes 21010-47021/47022
	    if(rangeHeader != null) {
		try {
		    String r = rangeHeader.getValue();
		    int l = Integer.parseInt(r.substring(v.indexOf('/') + 1));
		    this.status.setSize(l);
		}
		catch(NumberFormatException e) {
		    this.status.setSize(-1);
		}
	    }
//	    Header transferEncodingHeader = GET.getResponseHeader("Bookmark-Encoding");
//	    else if(null != transferEncodingHeader) {
//		if("chunked".equalsIgnoreCase(transferEncodingHeader.getValue())) {
//		    this.status.setSize(-1);
//		}
//	    }
//	    this.status.setSize(GET.getRequestContentLength());


	    OutputStream out = new FileOutputStream(this.getLocal(), this.status.isResume());
	    if(out == null) {
		throw new IOException("Unable to buffer data");
	    }
//	    session.log("Opening data stream...", Message.PROGRESS);
	    InputStream in = GET.getResponseBodyAsStream();
//	    InputStream in = session.HTTP.getInputStream(GET);
	    if(in == null) {
		throw new IOException("Unable opening data stream");
	    }
	    //session.log("Downloading "+this.getName(), Message.PROGRESS);
	    this.download(in, out);
	}
	catch(HttpException e) {
	    Header[] responseHeaders = GET.getResponseHeaders();
	    for(int i = 0; i < responseHeaders.length; i++) {
		session.log(responseHeaders[i].toExternalForm(), Message.TRANSCRIPT);
	    }
	    session.log(e.getReasonCode() + " " +  e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    session.log(e.getMessage(), Message.ERROR);
	}
    finally {
	session.log("Idle", Message.STOP);
//            try {
	GET.releaseConnection();
	session.close();
//	    }
//	    catch(IOException e) {
//		session.log(e.getMessage(), Message.ERROR);
//	    }
    }
}

    public void upload() {
	throw new IllegalArgumentException("Upload not supported");
    }

    public boolean isFile() {
	return true;
    }

    public boolean isDirectory() {
	return false;
    }
}
