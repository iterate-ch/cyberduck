package ch.cyberduck.connection.http;

/*
 *  ch.cyberduck.connection.http.HTTPConnection.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.*;

import com.enterprisedt.net.ftp.FTPTransferType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

/**
* Opens a connection to the remote server via http protocol
 */
public class HTTPSession extends Session {
    
    private HttpClient HTTP;

    /**
     * @param client The client to use which does implement the http protocol
     * @param action The <code>TransferAction</code> to execute after the connection has been opened
     * @param b The <code>Bookmark</code> object
     * @param secure If the connection is secure
     */
//    public HTTPSession(Client client, Bookmark b, TransferAction action, boolean secure) {
    public HTTPSession(Bookmark b, TransferAction action, boolean secure) {
        super(b, action, secure);
        this.HTTP = new HttpClient();
    }

    /**
     * Connect to the remote server and execute the action set
     * via <code>Session.setAction(TransferAction action)</code>
     * Must be ConnectAction.GET
     */
    public void run() {
        bookmark.status.fireActiveEvent();
        Cyberduck.DEBUG("[HTTPConnection] run():" + action.toString() + "************************************");
        GetMethod GET = null;
        try {
            this.log("Connecting to " + bookmark.getHost(), Status.PROGRESS);
        this.log("\nConnecting to " + bookmark.getIp()+"\n", Status.TRANSCRIPT);
            if(this.action.toString().equals(TransferAction.GET)) {
                if(Preferences.instance().getProperty("connection.proxy").equals("true")) {
                    HTTP.connect(bookmark.getHost(), bookmark.getPort(), Preferences.instance().getProperty("connection.proxy.host"), Integer.parseInt(Preferences.instance().getProperty("connection.proxy.port")));
                }
                else {
                    HTTP.connect(bookmark.getHost(), bookmark.getPort(), secure);
                }
                this.log("Connected.", Status.PROGRESS);
                this.check();
                
//                GET = new GetMethod(Path.encode(bookmark.getServerPathAsString()));
                GET = new GetMethod(bookmark.getServerPathAsString());
                GET.setUseDisk(false);

                if(Preferences.instance().getProperty("connection.proxy.authenticate").equals("true")) {
                     // enter the username and password for the proxy
                     String authString = Preferences.instance().getProperty(
                                                                 "connection.proxy.username")
                     +":"+
                     Preferences.instance().getProperty("connection.proxy.password");
                     // base64 encode the password.
                     String auth = "Basic " + Base64.encode(authString.getBytes());
                     // Set up the connection so it knows we are sending proxy user information
                     GET.addRequestHeader( "Proxy-Authorization", auth );
                }
                GET.addRequestHeader("Accept", "*/*");
                GET.addRequestHeader("User-Agent", "Cyberduck/" + Cyberduck.getVersion());
                if(bookmark.status.isResume()) {
                    GET.addRequestHeader("Range", "bytes=" + bookmark.status.getCurrent() + "-");
                }

                this.log("\n", Status.TRANSCRIPT);
                String p = GET.isHttp11() ? "HTTP/1.1" : "HTTP/1.0";
                this.log("GET " + bookmark.getServerPathAsString() + " " + p + "\n", Status.TRANSCRIPT);
                Header[] requestHeaders = GET.getRequestHeaders();
                for(int i = 0; i < requestHeaders.length; i++) {
                    this.log(requestHeaders[i].toExternalForm(), Status.TRANSCRIPT);
                }
                this.check();
                int response = HTTP.executeMethod(GET);
                this.log(response + " " + HttpStatus.getStatusText(response) + "\n", Status.TRANSCRIPT);
                Header[] responseHeaders = GET.getResponseHeaders();
                for(int i = 0; i < responseHeaders.length; i++) {
                    this.log(responseHeaders[i].toExternalForm(), Status.TRANSCRIPT);
                }
                this.log("\n", Status.TRANSCRIPT);

                if(response == HttpStatus.SC_MOVED_PERMANENTLY || response == HttpStatus.SC_MOVED_TEMPORARILY) {
                    try {
                        URL redirect = new URL(GET.getResponseHeader("Location").getValue());
                        bookmark.setAddress(redirect);
                        //bookmark.transfer(new TransferAction(TransferAction.GET));
                        return;
                    }
                    catch(MalformedURLException e) {
                        throw new HttpException(HttpStatus.getStatusText(response), response);
                    }
                }
                    
                if(!HttpStatus.isSuccessfulResponse(response)) {
                    throw new HttpException(HttpStatus.getStatusText(response), response);
                }
    
                if(bookmark.status.isResume()) {
                    if(GET.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
                        this.log("Resumption not possible.", Status.ERROR);
                        bookmark.status.setCurrent(0);
                        bookmark.status.setResume(false);
                    }
                    else {
                        this.log("Resume at " + bookmark.status.getCurrent() + ".", Status.PROGRESS);
                    }
                }
            
                Header rangeHeader = GET.getResponseHeader("Content-Range"); //Content-Range: bytes 21010-47021/47022
                Header lengthHeader = GET.getResponseHeader("Content-Length");
                Header transferEncodingHeader = GET.getResponseHeader("Bookmark-Encoding");
                if(lengthHeader != null) {
                    try {
                        bookmark.status.setLength(Integer.parseInt(lengthHeader.getValue()));
                    }
                    catch(NumberFormatException e) {
                        bookmark.status.setLength(-1);
                    }
                }
                if(rangeHeader != null) {
                    try {
                        String v = rangeHeader.getValue();
                        int l = Integer.parseInt(v.substring(v.indexOf('/') + 1));
                        bookmark.status.setLength(l);
                    }
                    catch(NumberFormatException e) {
                        bookmark.status.setLength(-1);
                    }
                }
                else if(null != transferEncodingHeader) {
                    if("chunked".equalsIgnoreCase(transferEncodingHeader.getValue())) {
                        bookmark.status.setLength(-1);
                    }
                }
    
                if(bookmark.getTransferType().equals(FTPTransferType.BINARY)) {
                    OutputStream out = new FileOutputStream(bookmark.getLocalTempPath().toString(), bookmark.status.isResume());
                    if(out == null) {
                        throw new IOException("Unable to buffer data");
                    }
                    this.check();
                    this.log("Opening data stream...", Status.PROGRESS);
                    InputStream in = HTTP.getInputStream(GET);
                    if(in == null) {
                        throw new IOException("Unable opening data stream");
                    }
                    this.log("Downloading "+bookmark.getServerFilename()+"...", Status.PROGRESS);
                    this.download(in, out);
                }
                else if(bookmark.getTransferType().equals(FTPTransferType.ASCII)) {
                    throw new IOException("ASCII transfers are not currently supported in HTTP mode");
                    //@todo: support ASCII http transfers.
                    /*
                    java.io.Writer out = new FileWriter(bookmark.getLocalTempPath().toString(), bookmark.bookmark.status.isResume());
                    if(out == null) {
                        throw new IOException("Unable to buffer data");
                    }
                    this.log("Opening data stream...", Message.PROGRESS);
                    java.io.Reader in = HTTP.getASCII(GET);
                    if(in == null) {
                        throw new IOException("Unable opening data stream");
                    }
                    this.transfer(in, out);
                    */
                }
                else {
                    throw new HttpException("Unknown transfer type");
                }
            }
            else {
                throw new HttpException("Unknown action: " + action.toString());
            }
        }
        catch(SessionException e) {
            this.log("Incomplete." , Status.PROGRESS);
            Header[] responseHeaders = GET.getResponseHeaders();
            for(int i = 0; i < responseHeaders.length; i++) {
                this.log(responseHeaders[i].toExternalForm(), Status.TRANSCRIPT);
            }
            this.log("\n", Status.TRANSCRIPT);
            this.log("HTTP Error: " + e.getReplyCode() + " " +  e.getMessage(), Status.ERROR);
        }
        catch (IOException e) {
            this.log("Incomplete." , Status.PROGRESS);
            this.log("IO Error: " + e.getMessage(), Status.ERROR);
        }
        finally {
            try {
                HTTP.quit();
            }
            catch(IOException e) {
                this.log(e.getMessage() , Status.ERROR);
            }
            this.saveLog();
            bookmark.status.fireStopEvent();
        }
    }
}
