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

import ch.cyberduck.core.Message;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import org.apache.log4j.Logger;

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
	if((null == parent)) {// && !abs.equals("/")) {
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

    public List list(boolean refresh) {
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

    public synchronized void download() {
	/*
	new Thread() {
	    public void run() {
		GetMethod GET = null;
		try {
		    HTTPPath.this.status.fireActiveEvent();
		    session.check();
		    GET = new GetMethod(HTTPPath.this.getAbsolute()); //@todo encode url
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
		    if(HTTPPath.this.status.isResume()) {
			GET.addRequestHeader("Range", "bytes=" + HTTPPath.this.status.getCurrent() + "-");
		    }
		    String v = GET.isHttp11() ? "HTTP/1.1" : "HTTP/1.0";
		    session.log("GET " + HTTPPath.this.getAbsolute() + " " + v, Message.TRANSCRIPT);
		    Header[] requestHeaders = GET.getRequestHeaders();
		    for(int i = 0; i < requestHeaders.length; i++) {
			session.log(requestHeaders[i].toExternalForm(), Message.TRANSCRIPT);
		    }
		    int response = HTTP.executeMethod(GET);
		    session.log(response + " " + HttpStatus.getStatusText(response), Message.TRANSCRIPT);
		    Header[] responseHeaders = GET.getResponseHeaders();
		    for(int i = 0; i < responseHeaders.length; i++) {
			session.log(responseHeaders[i].toExternalForm(), Message.TRANSCRIPT);
		    }
		    if(response == HttpStatus.SC_MOVED_PERMANENTLY || response == HttpStatus.SC_MOVED_TEMPORARILY) {
			    //@ todo
//			    try {

			//URL url = new URL(GET.getResponseHeader("Location").getValue());
			 //session.close();
				//@todo url.getQuery()
			// HTTPSession s = new HTTPSession(new Host(url.getProtocol(), url.getHost(), url.getPort());
			//	    HTTPPath redirect = new HTTPPath(url.getFile());
			//	    redirect.download();
			//	    return;

//			    }
//			    catch(MalformedURLException e) {
			throw new HttpException(HttpStatus.getStatusText(response), response);
//			    }
			}

		    if(!HttpStatus.isSuccessfulResponse(response)) {
			throw new HttpException(HttpStatus.getStatusText(response), response);
		    }

		    if(HTTPPath.this.status.isResume()) {
			if(GET.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
			    session.log("Resumption not possible.", Message.ERROR);
			    HTTPPath.this.status.setCurrent(0);
			    HTTPPath.this.status.setResume(false);
			}
			else {
			    session.log("Resume at " + HTTPPath.this.status.getCurrent() + ".", Message.PROGRESS);
			}
		    }

		    Header rangeHeader = GET.getResponseHeader("Content-Range"); //Content-Range: bytes 21010-47021/47022
		    Header lengthHeader = GET.getResponseHeader("Content-Length");
		    Header transferEncodingHeader = GET.getResponseHeader("Bookmark-Encoding");
		    if(lengthHeader != null) {
			try {
			    HTTPPath.this.status.setSize(Integer.parseInt(lengthHeader.getValue()));
			}
			catch(NumberFormatException e) {
			    HTTPPath.this.status.setSize(-1);
			}
		    }
		    if(rangeHeader != null) {
			try {
			    String r = rangeHeader.getValue();
			    int l = Integer.parseInt(r.substring(v.indexOf('/') + 1));
			    HTTPPath.this.status.setSize(l);
			}
			catch(NumberFormatException e) {
			    HTTPPath.this.status.setSize(-1);
			}
		    }
		    else if(null != transferEncodingHeader) {
			if("chunked".equalsIgnoreCase(transferEncodingHeader.getValue())) {
			    HTTPPath.this.status.setSize(-1);
			}
		    }

		    OutputStream out = new FileOutputStream(HTTPPath.this.getLocal(), HTTPPath.this.status.isResume());
		    if(out == null) {
			throw new IOException("Unable to buffer data");
		    }
		    session.log("Opening data stream...", Message.PROGRESS);
		    InputStream in = HTTP.getInputStream(GET);
		    if(in == null) {
			throw new IOException("Unable opening data stream");
		    }
		    session.log("Downloading "+HTTPPath.this.getName()+"...", Message.PROGRESS);
		    HTTPPath.this.download(in, out);
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
		}
	    }.start();
	 */
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

    public Session getSession() {
	return this.session;
    }
}
