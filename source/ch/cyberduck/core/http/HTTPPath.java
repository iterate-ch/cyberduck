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

import ch.cyberduck.core.*;

import com.apple.cocoa.foundation.NSDictionary;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

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
	
    public HTTPPath(HTTPSession session, String parent, Local file) {
		super(parent, file);
		this.session = session;
    }
    
	public HTTPPath(HTTPSession session, NSDictionary dict) {
		super(dict);
		this.session = session;
    }
	
    public Path copy(Session s) {
		HTTPPath copy = new HTTPPath((HTTPSession)s, this.getAbsolute());
//		HTTPPath copy = new HTTPPath((HTTPSession)s, this.getParent().getAbsolute(), this.getLocal());
		copy.attributes = this.attributes;
		//	copy.status = this.status;
		return copy;
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
	
    public void fillQueue(List queue, int kind) {
		log.debug("fillQueue:"+kind+","+kind);
		try {
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
	
    private void fillDownloadQueue(List queue) {
		queue.add(this);
    }
	
    public void download() {
		GetMethod GET = null;
		try {
			log.debug("download:"+this.toString());
			if(!this.isFile())
				throw new IOException("Download must be a file.");
			this.session.check();
			GET = new GetMethod(this.getAbsolute()); //@todo encode url
			GET.setUseDisk(false);
			GET.setFollowRedirects(false);
			GET.addRequestHeader("Accept", Preferences.instance().getProperty("http.acceptheader"));
			GET.addRequestHeader("User-Agent", Preferences.instance().getProperty("http.agent"));
			if(this.status.isResume()) {
				GET.addRequestHeader("Range", "bytes=" + this.status.getCurrent() + "-");
			}
			String v = GET.isHttp11() ? "HTTP/1.1" : "HTTP/1.0";
			session.log("GET " + this.getAbsolute() + " " + v, Message.TRANSCRIPT);
			Header[] requestHeaders = GET.getRequestHeaders();
			for(int i = 0; i < requestHeaders.length; i++) {
				session.log(requestHeaders[i].toString(), Message.TRANSCRIPT);
			}	    
			int response = session.HTTP.executeMethod(GET);
			session.log(response + " " + HttpStatus.getStatusText(response), Message.TRANSCRIPT);
			Header[] responseHeaders = GET.getResponseHeaders();
			for(int i = 0; i < responseHeaders.length; i++) {
				session.log(responseHeaders[i].toString(), Message.TRANSCRIPT);
			}
			if(!HttpStatus.isSuccessfulResponse(response)) {
				throw new HttpException(HttpStatus.getStatusText(response), response);
			}
			if(this.status.isResume()) {
				if(GET.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
					log.info("Resumption not possible.");
					//session.log("Resumption not possible.", Message.ERROR);
					this.status.setCurrent(0);
					this.status.setResume(false);
				}
				else {
					log.info("Resuming at " + this.status.getCurrent() + ".");
				}
			}
			Header lengthHeader = GET.getResponseHeader("Content-Length");
			if(lengthHeader != null) {
				try {
					this.status.setSize(Integer.parseInt(lengthHeader.getValue()));
				}
				catch(NumberFormatException e) {
					log.error(e.getMessage());
					this.status.setSize(-1);
				}
			}
			Header rangeHeader = GET.getResponseHeader("Content-Range"); //Content-Range: bytes 21010-47021/47022
			if(rangeHeader != null) {
				try {
					String rangeValue = rangeHeader.getValue();
					this.status.setSize(Integer.parseInt(rangeValue.substring(rangeValue.indexOf("/") + 1)));
				}
				catch(NumberFormatException e) {
					log.error(e.getMessage());
					this.status.setSize(-1);
				}
			}
			
			OutputStream out = new FileOutputStream(this.getLocal(), this.status.isResume());
			if(out == null) {
				throw new IOException("Unable to buffer data");
			}
			//	    session.log("Opening data stream...", Message.PROGRESS);
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
				session.log(responseHeaders[i].toString(), Message.TRANSCRIPT);
			}
			if(HttpStatus.SC_MOVED_TEMPORARILY == e.getReplyCode() || HttpStatus.SC_MOVED_PERMANENTLY == e.getReplyCode() || HttpStatus.SC_TEMPORARY_REDIRECT == e.getReplyCode()) {
				log.info("Processing redirect");
				try {
					URL redirect = new URL(GET.getResponseHeader("Location").getValue());
					this.session = new HTTPSession(new Host(redirect.getProtocol(), redirect.getHost(), redirect.getPort(), new Login(redirect.getUserInfo())));
					this.setPath(redirect.getFile());
					this.download();
					return;
				}
				catch(java.net.MalformedURLException me) {
					log.error(e.getMessage());
					//		    throw new HttpException(HttpStatus.getStatusText(e.getReplyCode()), e.getReplyCode());
				}
			}
			else
				session.log("HTTP Error: " + e.getReplyCode() + " " +  e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
			session.log(e.getMessage(), Message.ERROR);
		}
		finally {
			session.log("Idle", Message.STOP);
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
