package ch.cyberduck.core;

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

import java.io.*;
import java.util.Date;

import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class Session {//extends Thread {

    private static Logger log = Logger.getLogger(Session.class);

    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String FTP = "ftp";
    public static final String SFTP = "sftp";

    public static final int HTTP_PORT = 80;
    public static final int HTTPS_PORT = 443;
    public static final int FTP_PORT = 21;
    public static final int SSH_PORT = 22;

        
//    private Log log;

    public Host host;
//    public Bookmark bookmark;
    /**
     * the action to execute(download, upload, list, ...)
     */
//    public TransferAction action;

//    public boolean secure = false;

//    public Session(Bookmark b, TransferAction action) {
//	this(b, action, false);
//    }

    public Session(Host h) {//, TransferAction action) {//, boolean secure) {
	log.debug("Session("+h+")");
//        super(h.getName());
//        this.bookmark = b;
	this.host = h;
//	this.action = action;
//        this.secure = secure;
	
        System.getProperties().put("proxySet", Preferences.instance().getProperty("connection.proxy"));
        System.getProperties().put("proxyHost", Preferences.instance().getProperty("connection.proxy.host"));
        System.getProperties().put("proxyPort", Preferences.instance().getProperty("connection.proxy.port"));

	//@todo        this.log = new Log();
        this.log("-------" + new Date().toString(), Message.TRANSCRIPT);
        this.log("-------" + host.getName(), Message.TRANSCRIPT);
        this.log("-------" + host.getName(), Message.TRANSCRIPT);
    }

    /**
     * The protocol specific implementation has to  be coded in the subclasses. 
     */
    public abstract void connect();

    /**
	* The protocol specific implementation has to  be coded in the subclasses.
     */
    public abstract void close();

    /**
	* Assert that the connection to the remote host is still alive. Open connection if needed.
     * @throws IOException The connection to the remote host failed.
     */
    public abstract void check() throws IOException;

    
    /**
        * Can be called within the <code>run()</code> to check if the thread should die.
     */
//    public void check() {// throws SessionException {
	/*@todo
        if( bookmark.status.isCancled()) {
            bookmark.status.ignoreEvents(false);
            throw new SessionException("Session canceled.");
        }
	 */
//    }

    public void log(String message, String title) {
//        log.debug("[Session] log("+message+","+type+")");

	host.status.setMessage(message, title);

        if(title.equals(Message.TRANSCRIPT)) {
            Transcript.instance().transcript(message);
        }
	
	//	host.status.setMessage(host.getName()+System.getProperty("line.separator")+message, title);
	/*
//@todo        bookmark.status.setMessage(message, type);
        if(type.equals(Message.TRANSCRIPT)) {
            Transcript.instance().transcript(message);
        }
        if(type.equals(Status.LOG)) {
//@todo            log.append(message);
        }
        if(type.equals(Message.PROGRESS)) {
//@todo            log.append("       [PROGRESS] " + message);
        }
        if(type.equals(Message.ERROR)) {
//            log.append("       [ERROR] " + message);
            if(Preferences.instance().getProperty("interface.error-dialog").equals("true")) {
                StringBuffer error = new StringBuffer();
                //building lines with approx. 50 characters
                int begin = 0;
                int end = 50;
                while(end > 0 && end < message.length()) {
                    log.debug("***substring("+begin+","+end+")");
                    String sub = message.substring(begin, end);
                    int space = sub.lastIndexOf(' ');
                    log.debug("***append("+begin+","+(begin + space)+")");
                    error.append(message.substring(begin, (begin + space))+System.getProperty("line.separator"));
                    begin = (begin + space + 1);
                    end = (end + 30);
                }
                error.append(message.substring(begin));

                javax.swing.JOptionPane.showMessageDialog(
                                              null,
                                              error.toString(),
                                              host.getName(),
                                              javax.swing.JOptionPane.ERROR_MESSAGE,
                                              null
                                              );
            }
        }
	 */
    }
}
/*
    public void saveLog() {
//        if(Preferences.instance().getProperty("connection.log").equals("true")) {
//@todo            log.save();
    }
}
*/

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
