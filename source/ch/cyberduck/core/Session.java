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
import java.util.Observable;
import java.util.Observer;

/**
 * @version $Id$
 */
public abstract class Session extends Observable {
    private static Logger log = Logger.getLogger(Session.class);

    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String FTP = "ftp";
    public static final String SFTP = "sftp";

    /**
	* Default port for http
     */
    public static final int HTTP_PORT = 80;
    /**
	* Default port for https
     */
    public static final int HTTPS_PORT = 443;
    /**
	* Default port for ftp
     */
    public static final int FTP_PORT = 21;
    /**
	* Default port for ssh
     */
    public static final int SSH_PORT = 22;

    /**
	* Encapsulating all the information of the remote host
     */
    public Host host;

    private boolean connected;

    public Session(Host h) {//, TransferAction action) {//, boolean secure) {
	log.debug("Session("+h+")");
	this.host = h;
        this.log("-------" + new Date().toString(), Message.TRANSCRIPT);
        this.log("-------" + host.getIp(), Message.TRANSCRIPT);
    }

    public void callObservers(Object arg) {
        log.debug("callObservers:"+arg.toString());
	log.debug(this.countObservers()+" observer(s) known.");
	this.setChanged();
	this.notifyObservers(arg);
    }
    
    /**
	* Connect to the remote Host
     * The protocol specific implementation has to  be coded in the subclasses.
     * @see Host
     */
    public abstract void connect() throws IOException;

    /**
	* Connect to the remote host and mount the home directory
     */
    public abstract void mount() ;
    /**
	* Close the connecion to the remote host.
	* The protocol specific implementation has to  be coded in the subclasses.
     * @see Host
     */
    public abstract void close();

    /**
	* @return The current working directory (pwd)
     */
    public abstract Path workdir() throws IOException;
    /**
	* Assert that the connection to the remote host is still alive. Open connection if needed.
     * @throws IOException The connection to the remote host failed.
     * @see Host
     */
    public abstract void check() throws IOException;

    public abstract void download(Path download) throws IOException;

    public abstract void upload(java.io.File upload);
    
    /**
     * @return boolean True if the session has not yet been closed. 
     */
    public boolean isConnected() {
	return this.connected;
    }

    public void setConnected(boolean connected) {
	this.connected = connected;
//	host.callObservers(new Message(Message.CONNECTED));
    }
    
    public void log(String message, String title) {
//        log.debug("[Session] log("+message+","+type+")");
        if(title.equals(Message.TRANSCRIPT)) {
            Transcript.instance().transcript(message);
        }
	this.callObservers(new Message(title, message));

	
/*        if(type.equals(Status.LOG)) {
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
