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

        public List list() {
            HTTPSession.this.log("Invalid Operation", Message.ERROR);
	    return null;
        }

        public void delete() {
            HTTPSession.this.log("Invalid Operation", Message.ERROR);
        }
        public void rename(String filename) {
            HTTPSession.this.log("Invalid Operation", Message.ERROR);
        }
        public void mkdir() {
            HTTPSession.this.log("Invalid Operation", Message.ERROR);
        }

        public void download() {
	    GetMethod GET = null;
	    try {
		//                GET = new GetMethod(Path.encode(host.getServerPathAsString()));
		GET = new GetMethod(this.getAbsolute());
		GET.setUseDisk(false);

		if(Preferences.instance().getProperty("connection.proxy.authenticate").equals("true")) {
		    // enter the username and password for the proxy
		    String authString = Preferences.instance().getProperty("connection.proxy.username")+":"+Preferences.instance().getProperty("connection.proxy.password");
		    // base64 encode the password.
		    String auth = "Basic " + Base64.encode(authString.getBytes());
		    // Set up the connection so it knows we are sending proxy user information
		    GET.addRequestHeader( "Proxy-Authorization", auth );
		}
		GET.addRequestHeader("Accept", GET.getAcceptHeader());

		GET.addRequestHeader("User-Agent", "Cyberduck/" + Preferences.instance().getProperty("cyberduck.version"));
		if(host.status.isResume()) {
		    GET.addRequestHeader("Range", "bytes=" + host.status.getCurrent() + "-");
		}
		String v = GET.isHttp11() ? "HTTP/1.1" : "HTTP/1.0";
		HTTPSession.this.log("GET " + this.getAbsolute() + " " + v, Message.TRANSCRIPT);
		Header[] requestHeaders = GET.getRequestHeaders();
		for(int i = 0; i < requestHeaders.length; i++) {
		    HTTPSession.this.log(requestHeaders[i].toExternalForm(), Message.TRANSCRIPT);
		}
		//                this.check();
		int response = HTTP.executeMethod(GET);
		HTTPSession.this.log(response + " " + HttpStatus.getStatusText(response) + "\n", Message.TRANSCRIPT);
		Header[] responseHeaders = GET.getResponseHeaders();
		for(int i = 0; i < responseHeaders.length; i++) {
		    HTTPSession.this.log(responseHeaders[i].toExternalForm(), Message.TRANSCRIPT);
		}
		HTTPSession.this.log("\n", Message.TRANSCRIPT);

		if(response == HttpStatus.SC_MOVED_PERMANENTLY || response == HttpStatus.SC_MOVED_TEMPORARILY) {
		    try {
			URL redirect = new URL(GET.getResponseHeader("Location").getValue());
			//@todo                        host.setAddress(redirect);
   //host.transfer(new TransferAction(TransferAction.GET));
			return;
		    }
		    catch(MalformedURLException e) {
			throw new HttpException(HttpStatus.getStatusText(response), response);
		    }
		}

		if(!HttpStatus.isSuccessfulResponse(response)) {
		    throw new HttpException(HttpStatus.getStatusText(response), response);
		}

		if(host.status.isResume()) {
		    if(GET.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
			HTTPSession.this.log("Resumption not possible.", Message.ERROR);
			host.status.setCurrent(0);
			host.status.setResume(false);
		    }
		    else {
			HTTPSession.this.log("Resume at " + host.status.getCurrent() + ".", Message.PROGRESS);
		    }
		}

		Header rangeHeader = GET.getResponseHeader("Content-Range"); //Content-Range: bytes 21010-47021/47022
		Header lengthHeader = GET.getResponseHeader("Content-Length");
		Header transferEncodingHeader = GET.getResponseHeader("Bookmark-Encoding");
		if(lengthHeader != null) {
		    try {
			host.status.setLength(Integer.parseInt(lengthHeader.getValue()));
		    }
		    catch(NumberFormatException e) {
			host.status.setLength(-1);
		    }
		}
		if(rangeHeader != null) {
		    try {
			String r = rangeHeader.getValue();
			int l = Integer.parseInt(r.substring(v.indexOf('/') + 1));
			host.status.setLength(l);
		    }
		    catch(NumberFormatException e) {
			host.status.setLength(-1);
		    }
		}
		else if(null != transferEncodingHeader) {
		    if("chunked".equalsIgnoreCase(transferEncodingHeader.getValue())) {
			host.status.setLength(-1);
		    }
		}
		/*
		 //@todo		OutputStream out = new FileOutputStream(host.getLocalTempPath().toString(), host.status.isResume());
		 if(out == null) {
		     throw new IOException("Unable to buffer data");
		 }
		 //		this.check();
		 this.log("Opening data stream...", Message.PROGRESS);
		 InputStream in = HTTP.getInputStream(GET);
		 if(in == null) {
		     throw new IOException("Unable opening data stream");
		 }
		 this.log("Downloading "+host.getServerFilename()+"...", Message.PROGRESS);
		 this.download(in, out);
	    }
		 */
		//            else {
  //                throw new HttpException("Unknown action: " + action.toString());
  //            }
	}
	    catch(IOException e) {
		HTTPSession.this.log(e.getMessage(), Message.ERROR);
	    }
	    catch(HttpException e) {
		Header[] responseHeaders = GET.getResponseHeaders();
		for(int i = 0; i < responseHeaders.length; i++) {
		    HTTPSession.this.log(responseHeaders[i].toExternalForm(), Message.TRANSCRIPT);
		}
		HTTPSession.this.log(e.getReplyCode() + " " +  e.getMessage(), Message.ERROR);
	    }
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

    /**
     * @param client The client to use which does implement the http protocol
     * @param action The <code>TransferAction</code> to execute after the connection has been opened
     * @param b The <code>Bookmark</code> object
     * @param secure If the connection is secure
     */
    public HTTPSession(Host h) {//, TransferAction action) {
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

    /**
     * Connect to the remote server and execute the action set
     * via <code>Session.setAction(TransferAction action)</code>
     * Must be ConnectAction.GET
     */
    public void run() {
	host.status.fireActiveEvent();
	this.log("Opening HTTP connection to " + host.getIp() +"...", Message.PROGRESS);
	//            if(this.action.toString().equals(TransferAction.GET)) {
	if(Preferences.instance().getProperty("connection.proxy").equals("true")) {
	    HTTP.connect(host.getName(), host.getPort(), Preferences.instance().getProperty("connection.proxy.host"), Integer.parseInt(Preferences.instance().getProperty("connection.proxy.port")));
	}
	else {
	    HTTP.connect(host.getName(), host.getPort(), false);//@todo implement https
	}
	this.log("HTTP connection opened", Message.PROGRESS);
	//@todo     	           this.check();
	new HTTPFile(host.getPath()).download();
    }
}
