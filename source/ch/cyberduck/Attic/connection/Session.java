package ch.cyberduck.connection;

/*
 *  ch.cyberduck.connection.Session.java
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

import java.io.*;
import java.util.Date;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;

/**
 * @version $Id$
 */
public abstract class Session extends Thread {

    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String FTP = "ftp";
    public static final String SFTP = "sftp";

    public static final int HTTP_PORT = 80;
    public static final int HTTPS_PORT = 443;
    public static final int FTP_PORT = 21;
    public static final int SFTP_PORT = 22;
        
    private Log log;

    public Bookmark bookmark;
    /**
     * the action to execute(download, upload, list, ...)
     */
    public TransferAction action;

    public boolean secure = false;

    public Session(Bookmark b, TransferAction action) {
	this(b, action, false);
    }
    public Session(Bookmark b, TransferAction action, boolean secure) {
        super(b.getHost());
        this.bookmark = b;
        this.action = action;
        this.secure = secure;
        System.getProperties().put("proxySet", Preferences.instance().getProperty("connection.proxy"));
        System.getProperties().put("proxyHost", Preferences.instance().getProperty("connection.proxy.host"));
        System.getProperties().put("proxyPort", Preferences.instance().getProperty("connection.proxy.port"));
        this.log = new Log();
        this.log("-------" + new Date().toString(), Status.LOG);
        this.log("-------" + bookmark.getAddressAsString(), Status.LOG);
    }

    /**
     * Start the session and run the action specified with <code>TransferAction</code>
     * in the constructor.
     * The protocol specific implementation is be coded in the subclasses. 
     */
    public abstract void run();

    /**
     * ascii upload
     * @param reader The stream to read from
     * @param writer The stream to write to
     */
    public void upload(java.io.Writer writer, java.io.Reader reader) throws IOException {
        Cyberduck.DEBUG("[Session] upload(" + writer.toString() + ", " + reader.toString());
  //      this.log("Uploading " + action.getParam() + "... (ASCII)", Status.PROGRESS);
        this.transfer(reader, writer);
//        this.log("Upload of '" + action.getParam() + "' complete", Status.PROGRESS);
    }

    /**
     * binary upload
     * @param i The stream to read from
     * @param o The stream to write to
     */
    public void upload(java.io.OutputStream o, java.io.InputStream i) throws IOException {
        Cyberduck.DEBUG("[Session] upload(" + o.toString() + ", " + i.toString());
//        this.log("Uploading " + action.getParam() + "... (BINARY)", Status.PROGRESS);
        this.transfer(i, o);
//        this.log("Upload of '" + action.getParam() + "' complete", Status.PROGRESS);
    }

    /**
     * ascii download
     * @param reader The stream to read from
     * @param writer The stream to write to
     */
    public void download(java.io.Reader reader, java.io.Writer writer) throws IOException {
        Cyberduck.DEBUG("[Session] transfer(" + reader.toString() + ", " + writer.toString());
//        this.log("Downloading " + bookmark.getServerFilename() + "... (ASCII)", Status.PROGRESS);
        this.transfer(reader, writer);
    }

    /**
     * binary download
     * @param i The stream to read from
     * @param o The stream to write to
     */
    public void download(java.io.InputStream i, java.io.OutputStream o) throws IOException {
        Cyberduck.DEBUG("[Session] transfer(" + i.toString() + ", " + o.toString());
//        this.log("Downloading " + bookmark.getServerFilename() + "... (BINARY) ", Status.PROGRESS);
        this.transfer(i, o);
    }

    /**
    * @param reader The stream to read from
        * @param writer The stream to write to
     */
    private void transfer(java.io.Reader reader, java.io.Writer writer) throws IOException {
        LineNumberReader in = new LineNumberReader(reader);
        BufferedWriter out = new BufferedWriter(writer);
        int current = bookmark.status.getCurrent();
        boolean complete = false;
        // read/write a line at a time
        String line = null;
        while (!complete && !bookmark.status.isCancled()) {
            line = in.readLine();
            if(line == null) {
                complete = true;
            }
            else {
                bookmark.status.setCurrent(current += line.getBytes().length);
                out.write(line, 0, line.length());
                out.newLine();
            }
        }
        this.eof(complete);
        // close streams
        if(in != null) {
            in.close();
        }
        if(out != null) {
            out.flush();
            out.close();
        }
    }

    /**
        * @param i The stream to read from
     * @param o The stream to write to
     */
    private void transfer(java.io.InputStream i, java.io.OutputStream o) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new DataInputStream(i));
        BufferedOutputStream out = new BufferedOutputStream(new DataOutputStream(o));

        // do the retrieving
        int chunksize = Integer.parseInt(Preferences.instance().getProperty("connection.buffer"));
        byte[] chunk = new byte[chunksize];
        int amount = 0;
        int current = bookmark.status.getCurrent();
        boolean complete = false;

        // read from socket (bytes) & write to file in chunks
        while (!complete && !bookmark.status.isCancled()) {
            amount = in.read(chunk, 0, chunksize);
            if(amount == -1) {
                complete = true;
            }
            else {
                bookmark.status.setCurrent(current += amount);
                out.write(chunk, 0, amount);
            }
        }
        this.eof(complete);
        // close streams
        if(in != null) {
            in.close();
        }
        if(out != null) {
            out.flush();
            out.close();
        }
    }

    /**
     * Do some cleanup if transfer has been completed
     */
    private void eof(boolean complete) {
        if(complete) {
            bookmark.status.setCurrent(bookmark.status.getLength());
            if(action.toString().equals(TransferAction.GET)) {
                bookmark.getLocalTempPath().renameTo(bookmark.getLocalPath());
                if(Preferences.instance().getProperty("files.postprocess").equals("true")) {
                    bookmark.open();
                }
            }
            this.log("Complete" , Status.PROGRESS);
            bookmark.status.fireCompleteEvent();
        }
        else {
            this.log("Incomplete", Status.PROGRESS);
            bookmark.status.fireStopEvent();
        }
    }

    /**
        * Can be called within the <code>run()</code> to check if the thread should die.
     */
    public void check() throws SessionException {
        if( bookmark.status.isCancled()) {
            bookmark.status.ignoreEvents(false);
            throw new SessionException("Session canceled.");
        }
    }

    public void log(String message, Message type) {
        Cyberduck.DEBUG("[Session] log("+message+","+type+")");
        bookmark.status.setMessage(message, type);
        if(type.equals(Status.TRANSCRIPT)) {
            Transcript.instance().transcript(message);
        }
        if(type.equals(Status.LOG)) {
            log.append(message);
        }
        if(type.equals(Status.PROGRESS)) {
            log.append("       [PROGRESS] " + message);
        }
        if(type.equals(Status.ERROR)) {
            log.append("       [ERROR] " + message);
            if(Preferences.instance().getProperty("interface.error-dialog").equals("true")) {
                StringBuffer error = new StringBuffer();
                //building lines with approx. 50 characters
                int begin = 0;
                int end = 50;
                while(end > 0 && end < message.length()) {
                    Cyberduck.DEBUG("***substring("+begin+","+end+")");
                    String sub = message.substring(begin, end);
                    int space = sub.lastIndexOf(' ');
                    Cyberduck.DEBUG("***append("+begin+","+(begin + space)+")");
                    error.append(message.substring(begin, (begin + space))+System.getProperty("line.separator"));
                    begin = (begin + space + 1);
                    end = (end + 30);
                }
                error.append(message.substring(begin));

                javax.swing.JOptionPane.showMessageDialog(
                                              null,
                                              error.toString(),
                                              bookmark.getHost(),
                                              javax.swing.JOptionPane.ERROR_MESSAGE,
                                              null
                                              );
            }
        }
    }

    public void saveLog() {
//        if(Preferences.instance().getProperty("connection.log").equals("true")) {
            log.save();
    }

/*
import java.util.*;

class SystemProperties
{
    public static void main(String args[])
    {
        Properties systemproperties = System.getProperties();
        systemproperties.put("firewallHost",
                             "web.proxy.nl.com"); // firewall proxy server
        systemproperties.put("firewallPort",
                             "140");              // firewall port #
        systemproperties.put("firewallSet","true");
        systemproperties.put("proxyHost",
                             "http.proxy.nl.com");// http proxy server
        systemproperties.put("proxyPort",
                             "180");              // http port #
        systemproperties.put("proxySet","true");
        systemproperties.put("ftpProxyHost",
                             "ftp.proxy.nl.com"); // ftp proxy server
        systemproperties.put("ftpProxyPort",
                             "110");              // ftp port #
        systemproperties.put("ftpProxySet","true");
        systemproperties.put("http.nonProxyHosts",
                             "apple.com|netscape.com");
                                                  // proxy bypass sites
        System.setProperties(systemproperties);
    }
}
     */
}
