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
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.Session;
import org.apache.log4j.Logger;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import ch.cyberduck.core.Preferences;

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
    
    public List list(boolean notifyobservers) {
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

//  public int size() {
//	return this.status.getSize();
//    }

    public void changePermissions(int p) {
	session.log("Invalid Operation", Message.ERROR);
    }

    public void changeOwner(String owner) {
	session.log("Invalid Operation", Message.ERROR);
    }

    public void changeGroup(String group) {
	session.log("Invalid Operation", Message.ERROR);
    }
    
    public void fillDownloadQueue(Queue queue, Session session) {
	queue.add(this);
    }
    
    public void download() {
	GetMethod GET = null;
	try {
	    log.debug("download:"+this.toString());
//	    this.status.fireActiveEvent();
	    session.check();
	    GET = new GetMethod(this.getAbsolute()); //@todo encode url
	    GET.setUseDisk(false);
//	@todo proxy		if(Preferences.instance().getProperty("connection.proxy.authenticate").equals("true")) {
		    // enter the username and password for the proxy
//			    String authString = Preferences.instance().getProperty("connection.proxy.username")+":"+Preferences.instance().getProperty("connection.proxy.password");
		    // base64 encode the password.
//			    String auth = "Basic " + Base64.encode(authString.getBytes());
		    // Set up the connection so it knows we are sending proxy user information
//			    GET.addRequestHeader( "Proxy-Authorization", auth );
//			}
	    GET.addRequestHeader("Accept", GET.getAcceptHeader());
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
	    int response = session.HTTP.executeMethod(GET);
	    session.log(response + " " + HttpStatus.getStatusText(response), Message.TRANSCRIPT);
	    Header[] responseHeaders = GET.getResponseHeaders();
	    for(int i = 0; i < responseHeaders.length; i++) {
		session.log(responseHeaders[i].toExternalForm(), Message.TRANSCRIPT);
	    }
	    if(response == HttpStatus.SC_MOVED_PERMANENTLY || response == HttpStatus.SC_MOVED_TEMPORARILY) {
		//what about observers?
		/*
		try {
		    URL url = new URL(GET.getResponseHeader("Location").getValue());
		    session.close();
		    log.debug("IMPLEMENT URL QUERY!!! "+url.getQuery());
			//@todo url.getQuery()
		    session.HTTPSession s = new session.HTTPSession(new Host(url.getProtocol(), url.getHost(), url.getPort()));
		    session.HTTPPath redirect = new session.HTTPPath(s, url.getFile());
		    redirect.download();
		    return;
		}
		catch(MalformedURLException e) {
		    throw new HttpException(HttpStatus.getStatusText(response), response);
		}
		 */
	    }

	    if(!HttpStatus.isSuccessfulResponse(response)) {
		throw new HttpException(HttpStatus.getStatusText(response), response);
	    }

	    if(this.status.isResume()) {
		if(GET.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
		    session.log("Resumption not possible.", Message.ERROR);
		    this.status.setCurrent(0);
		    this.status.setResume(false);
		}
		else {
		    session.log("Resume at " + this.status.getCurrent() + ".", Message.PROGRESS);
		}
	    }

	    Header rangeHeader = GET.getResponseHeader("Content-Range"); //Content-Range: bytes 21010-47021/47022
	    Header lengthHeader = GET.getResponseHeader("Content-Length");
	    Header transferEncodingHeader = GET.getResponseHeader("Bookmark-Encoding");
	    if(lengthHeader != null) {
		try {
		    this.status.setSize(Integer.parseInt(lengthHeader.getValue()));
		}
		catch(NumberFormatException e) {
		    this.status.setSize(-1);
		}
	    }
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
	    else if(null != transferEncodingHeader) {
		if("chunked".equalsIgnoreCase(transferEncodingHeader.getValue())) {
		    this.status.setSize(-1);
		}
	    }

	    OutputStream out = new FileOutputStream(this.getLocal(), this.status.isResume());
	    if(out == null) {
		throw new IOException("Unable to buffer data");
	    }
	    session.log("Opening data stream...", Message.PROGRESS);
	    InputStream in = session.HTTP.getInputStream(GET);
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
	    session.log(e.getReplyCode() + " " +  e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    session.log(e.getMessage(), Message.ERROR);
	}
	finally {
            try {
		session.HTTP.quit();
            }
            catch(IOException e) {
		session.log(e.getMessage(), Message.ERROR);
            }
	}
    }

    public void fillUploadQueue(Queue queue, Session session) {
	session.log("Invalid Operation", Message.ERROR);
    }
    
    public void upload() {
	session.log("Invalid Operation", Message.ERROR);
    }

    public boolean isFile() {
	return true;
    }

    public boolean isDirectory() {
	return false;
    }
}
