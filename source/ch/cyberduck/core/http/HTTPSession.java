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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import ch.cyberduck.core.Preferences;import ch.cyberduck.core.*;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPTransferType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

/**
* Opens a connection to the remote server via http protocol
 * @version $Id$
 */
public class HTTPSession extends Session {

    private static Logger log = Logger.getLogger(Session.class);

    class HTTPFile extends Path {

        public HTTPFile(String parent, String name) {
            super(parent, name);
        }

        public HTTPFile(String path) {
            super(path);
        }

        public Path getParent() {
            String abs = this.getAbsolute();
            if((null == parent) && !abs.equals("/")) {
                int index = abs.lastIndexOf('/');
                String dirname = abs;
                if(index > 0)
                    dirname = abs.substring(0, index);
                if(index == 0) //parent is root
                    dirname = "/";
                parent = new HTTPFile(dirname);
            }
            log.debug("getParent:"+parent);
            return parent;
        }

        public void list() {
            HTTPSession.this.log("Invalid Operation", Message.ERROR);
        }

        public void delete() {
            HTTPSession.this.log("Invalid Operation", Message.ERROR);
        }
        public void rename(String filename) {
            HTTPSession.this.log("Invalid Operation", Message.ERROR);
        }
        public void mkdir(String name) {
            HTTPSession.this.log("Invalid Operation", Message.ERROR);
        }

	public void changePermissions(int p) {
            HTTPSession.this.log("Invalid Operation", Message.ERROR);
	}

        public void download() {
	    new Thread() {
		public void run() {
		    GetMethod GET = null;
		    try {
			HTTPFile.this.status.fireActiveEvent();
			HTTPSession.this.check();
			GET = new GetMethod(HTTPFile.this.getAbsolute()); //@todo encode url
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
			if(HTTPFile.this.status.isResume()) {
			    GET.addRequestHeader("Range", "bytes=" + HTTPFile.this.status.getCurrent() + "-");
			}
			String v = GET.isHttp11() ? "HTTP/1.1" : "HTTP/1.0";
			HTTPSession.this.log("GET " + HTTPFile.this.getAbsolute() + " " + v, Message.TRANSCRIPT);
			Header[] requestHeaders = GET.getRequestHeaders();
			for(int i = 0; i < requestHeaders.length; i++) {
			    HTTPSession.this.log(requestHeaders[i].toExternalForm(), Message.TRANSCRIPT);
			}
			int response = HTTP.executeMethod(GET);
			HTTPSession.this.log(response + " " + HttpStatus.getStatusText(response), Message.TRANSCRIPT);
			Header[] responseHeaders = GET.getResponseHeaders();
			for(int i = 0; i < responseHeaders.length; i++) {
			    HTTPSession.this.log(responseHeaders[i].toExternalForm(), Message.TRANSCRIPT);
			}
			if(response == HttpStatus.SC_MOVED_PERMANENTLY || response == HttpStatus.SC_MOVED_TEMPORARILY) {
			    try {
				URL url = new URL(GET.getResponseHeader("Location").getValue());
				HTTPSession.this.close();
				//@todo url.getQuery()
				Host host = new Host(url.getProtocol(), url.getHost(), url.getPort(), url.getPath(), null);
				HTTPFile redirect = new HTTPFile(host.getWorkdir());
				HTTPFile.this.download();
				return;
			    }
			    catch(MalformedURLException e) {
				throw new HttpException(HttpStatus.getStatusText(response), response);
			    }
			}

			if(!HttpStatus.isSuccessfulResponse(response)) {
			    throw new HttpException(HttpStatus.getStatusText(response), response);
			}

			if(HTTPFile.this.status.isResume()) {
			    if(GET.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
				HTTPSession.this.log("Resumption not possible.", Message.ERROR);
				HTTPFile.this.status.setCurrent(0);
				HTTPFile.this.status.setResume(false);
			    }
			    else {
				HTTPSession.this.log("Resume at " + HTTPFile.this.status.getCurrent() + ".", Message.PROGRESS);
			    }
			}

			Header rangeHeader = GET.getResponseHeader("Content-Range"); //Content-Range: bytes 21010-47021/47022
			Header lengthHeader = GET.getResponseHeader("Content-Length");
			Header transferEncodingHeader = GET.getResponseHeader("Bookmark-Encoding");
			if(lengthHeader != null) {
			    try {
				HTTPFile.this.attributes.setSize(Integer.parseInt(lengthHeader.getValue()));
			    }
			    catch(NumberFormatException e) {
				HTTPFile.this.attributes.setSize(-1);
			    }
			}
			if(rangeHeader != null) {
			    try {
				String r = rangeHeader.getValue();
				int l = Integer.parseInt(r.substring(v.indexOf('/') + 1));
				HTTPFile.this.attributes.setSize(l);
			    }
			    catch(NumberFormatException e) {
				HTTPFile.this.attributes.setSize(-1);
			    }
			}
			else if(null != transferEncodingHeader) {
			    if("chunked".equalsIgnoreCase(transferEncodingHeader.getValue())) {
				HTTPFile.this.attributes.setSize(-1);
			    }
			}

			OutputStream out = new FileOutputStream(HTTPFile.this.getLocal(), HTTPFile.this.status.isResume());
			if(out == null) {
			    throw new IOException("Unable to buffer data");
			 }	
			 HTTPSession.this.log("Opening data stream...", Message.PROGRESS);
			 InputStream in = HTTP.getInputStream(GET);
			 if(in == null) {
			     throw new IOException("Unable opening data stream");
			 }
			 HTTPSession.this.log("Downloading "+HTTPFile.this.getName()+"...", Message.PROGRESS);
			 HTTPFile.this.download(in, out);
		    }
		    catch(HttpException e) {
			Header[] responseHeaders = GET.getResponseHeaders();
			for(int i = 0; i < responseHeaders.length; i++) {
			    HTTPSession.this.log(responseHeaders[i].toExternalForm(), Message.TRANSCRIPT);
			}
			HTTPSession.this.log(e.getReplyCode() + " " +  e.getMessage(), Message.ERROR);
		    }
		    catch(IOException e) {
			HTTPSession.this.log(e.getMessage(), Message.ERROR);
		    }
		}
	    }.start();
	}

        public void upload() {
            HTTPSession.this.log("Invalid Operation", Message.ERROR);
        }

	public boolean isFile() {
	    return true;
	}

	public boolean isDirectory() {
	    return false;
	}
    }

    private HttpClient HTTP;


    public HTTPSession(Host h) {
        super(h);
        this.HTTP = new HttpClient();
    }

    public void close() {
	try {
	    HTTP.quit();
	}
	catch(IOException e) {
	    e.printStackTrace();
	}
	host.status.fireStopEvent();
    }

    public void check() throws IOException {
	if(!HTTP.isAlive())
	    this.connect();
    }
	

    public void connect() {
	new Thread() {
	    public void run() {
		host.status.fireActiveEvent();
		HTTPSession.this.log("Opening HTTP connection to " + host.getIp() +"...", Message.PROGRESS);
//		if(Preferences.instance().getProperty("connection.proxy").equals("true")) {
//		    HTTP.connect(host.getName(), host.getPort(), Preferences.instance().getProperty("connection.proxy.host"), Integer.parseInt(Preferences.instance().getProperty("connection.proxy.port")));
//		}
//		else {
		HTTP.connect(host.getName(), host.getPort(), false);//@todo implement https
//		}
		HTTPSession.this.log("HTTP connection opened", Message.PROGRESS);
		HTTPFile p = new HTTPFile(host.getWorkdir());
		p.download();
		host.status.fireStopEvent();
	    }
	}.start();
    }
}