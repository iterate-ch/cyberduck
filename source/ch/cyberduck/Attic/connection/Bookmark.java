package ch.cyberduck.connection;

/*
 *  ch.cyberduck.connection.Bookmark.java
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import com.enterprisedt.net.ftp.FTPTransferType;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.sftp.SFTPSession;
import ch.cyberduck.connection.ftp.FTPSession;
import ch.cyberduck.connection.http.HTTPSession;

/**
 * I'm a bookmark representing a download, listing or upload.
 * @version $Id$
 */
public class Bookmark extends Observable implements Serializable {

    private transient Queue queue;
    private transient Session session;
//    private transient Client client;
    /**
     * Encapsulating all status attributes of this bookmark
     */
    public Status status;

//    private transient javax.swing.JDialog statusDialog;
    
    private List listing = new ArrayList();
    
    private List pathHistory = new ArrayList();

    private URL address;
    private String host;
    private String protocol = Preferences.instance().getProperty("connection.protocol.default");
    private int port = -1;
    
    private String username = Preferences.instance().getProperty("ftp.login.anonymous.name");
    private transient String passwd = Preferences.instance().getProperty("ftp.login.anonymous.pass");

    private FTPTransferType transfertype = FTPTransferType.BINARY;
    
    private String serverFilename;
    
    private String localFilename;
    private File localTempPath;
    private File localFinalPath;
    private File localDirectory;

    /**
     * The full server path
     */
    private Path serverPath;
    /**
     * The directory part of this full server path
     */
    private Path serverDirectory;
    /**
     * the current working directory
     */
    private Path currentPath;
    /**
     * Mesage sent to the observers if this bookmark is selected
     */
    public static final Message SELECTION = new Message("SELECTION");
    /**
        * Mesage sent to the observers when this bookmark gets cleaned up 
     */
    public static final Message DESELECTION = new Message("DESELECTION");
    /**
        * Mesage sent to the observers when this bookmark 
        * has been edited (e.g. change the server path)
     */
    public static final Message EDIT = new Message("EDIT");
    public static final Message LIST = new Message("LIST");
    
    /**
     * Create a empty bookmark with default values
     */
    public Bookmark() {
	System.out.println("init bookmark");
        this.status = new Status();
        this.setPort(-1);
//        this.setTransferType(null);
    }

    /*
     * Create a bookmark with values from URL
     */
    public Bookmark(URL address) {
        this();
        this.setAddress(address);
    }

    /**
     * @return A copy of this transfer.
     */
    public Bookmark copy() {
        Bookmark copy = null;
        try {
            copy = new Bookmark();
            copy.setAddress(this.getProtocol(),
                            this.getHost(),
                            this.getPort(),
                            this.getServerPathAsString(),
                            this.getUsername(),
                            this.getPassword()
                            );
        }
        catch(MalformedURLException e) {
            e.printStackTrace();
        }
        return copy;
    }

    /**
     * @param arg The <code>Message</code to notifiy the observers of. 
     */
    public void callObservers(Object arg) {
        this.setChanged();
        if(this.status.isSelected()) {
            this.notifyObservers(arg);
        }
    }
    
    public void setAddress(String protocol, String host, int port, String serverPath, File localPath)
throws MalformedURLException {
        this.setAddress(new URL(protocol, host, port, serverPath));
        this.setLocalPath(localPath);
    }

    public void setAddress(String protocol, String host, int port, String serverPath, File localPath, String user, String pass)
throws MalformedURLException {
        this.setAddress(protocol, host, port, serverPath, localPath);
        this.setUsername(user);
        this.setPassword(pass);
    }
    
    public void setAddress(String protocol, String host, int port, String serverPath) throws MalformedURLException {
        this.setAddress(new URL(protocol, host, port, serverPath));
    }

    public void setAddress(String protocol, String host, int port, String serverPath, String user, String pass) throws MalformedURLException {
        this.setAddress(protocol, host, port, serverPath);
        this.setUsername(user);
        this.setPassword(pass);
    }

    public void setAddress(String protocol, String host, String serverPath) throws MalformedURLException {
        this.setAddress(new URL(protocol, host, serverPath));
    }

    public void setAddress(String protocol, String host, String serverPath, String user, String pass) throws MalformedURLException {
        this.setAddress(protocol, host, serverPath);
        this.setUsername(user);
        this.setPassword(pass);
    }
    
    public void setAddress(String host, String serverPath) throws MalformedURLException {
        this.setAddress(new URL(Preferences.instance().getProperty("connection.protocol.default"), host, serverPath));
    }

    public void setAddress(String host, String serverPath, String user, String pass) throws MalformedURLException {
        this.setAddress(host, serverPath);
        this.setUsername(user);
        this.setPassword(pass);
    }
    
    public void setAddress(URL a) {
        Cyberduck.DEBUG("[Bookmark] setAddress(" + a.toString() + ")");
        this.address = a;
        this.setHost(a.getHost());
        this.setProtocol(a.getProtocol());
        this.setPort(a.getPort());
        if(a.getQuery() != null) {
            this.setServerPath(a.getPath() + "?" + a.getQuery());
            this.setLocalPath(new File(Preferences.instance().getProperty("download.path"), a.getQuery()));
        }
        else
            this.setServerPath(a.getPath());
        this.setUserInfo(a.getUserInfo());
        this.callObservers(Bookmark.SELECTION);
    }

    public void setAddress(String address_string) {
        URL address_url = null;
        if(! (address_string.equals(""))) {
            try {
                address_url = new URL(address_string);
            }
            catch(MalformedURLException malformed) {
                try {
                    address_url = new URL(Preferences.instance().getProperty("connection.protocol.default") + "://" + address_string);
                }
                catch(MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            this.setAddress(address_url);
        }
    }

    /**
     * Get a url of this transfer.
     * @return null when there's missing information to construct a valid url.
     */
    public URL getAddress() {
        if(this.isValid()) {
            try {
//	    	return this.getProtocol()+"://"+this.getHost()+this.getServerPathAsString();
		return new URL(this.getProtocol(), this.getHost(), this.getServerPathAsString());
	    }
          catch(MalformedURLException e) {
	      e.printStackTrace();
	  }
        }
        return null;
    }

    public String getAddressAsString() {
        if(this.isValid())
            return this.getAddress().toExternalForm();
        else
            return "<URL unknown>";
    }

    public String getURLAsString() {
        if(this.getProtocol().equals(Session.HTTP) || this.getProtocol().equals(Session.HTTPS))
            return this.getAddressAsString();
        if(this.isValid())
            return this.getProtocol()+"://"+this.getUsername()+"@"+this.getHost()+this.getServerPathAsString();
        else
            return "<URL unknown>";
    }
    
    public void setProtocol(String p) {
        this.protocol = p;
    }
    
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Set all server pathnames
     * @param p The String of the absolute server path. Include '/' at the end to indicate a directory.
     */
    public void setServerPath(String p) {
        Cyberduck.DEBUG("[Bookmark] setServerPath("+ p.toString() + ")");
        if(p == null || p.equals("")) {
            this.serverPath = new Path("/");
            this.serverDirectory = serverPath;
        }
        else {
            this.serverPath = new Path(p);
            if(serverPath.isDirectory()) {
                this.serverDirectory = serverPath;
                this.serverFilename = null;
                this.localFilename = null;
                this.localFinalPath = null;
                this.localTempPath = null;
                this.localDirectory = null;
            }
            else {
                this.serverDirectory = serverPath.getParent();
                //this.serverFilename = Path.encode(serverPath.getName());
                this.serverFilename = serverPath.getName();
                this.setLocalPath(new File(Preferences.instance().getProperty("download.path"), this.serverFilename));
            }
        }
        this.callObservers(Bookmark.SELECTION);
        //Cyberduck.DEBUG(this.toString());
    }
    
    public Path getServerPath() {
//        Cyberduck.DEBUG("[Bookmark] getServerPath():return " + serverPath);
        return this.serverPath;
    }

    public String getServerPathAsString() {
//        Cyberduck.DEBUG("[Bookmark] getServerPath():return " + serverPath);
        if(this.serverPath == null)
            return "";
        return this.getServerPath().toString();
    }

    public void setCurrentPath(Path p) {
        Cyberduck.DEBUG("[Bookmark] setCurrentPath("+p+")");
        this.currentPath = p;
        this.addHistoryPath(p);
    }

    public Path getCurrentPath() {
        if(this.currentPath == null)
            return this.getServerPath();
        return this.currentPath;
    }
    
    public String getCurrentPathAsString() {
        if(this.getCurrentPath() == null)
            return "";
        return this.getCurrentPath().toString();
    }
    
    public Path getServerDirectory() {
//        Cyberduck.DEBUG("[Bookmark] getServerDirectory():return " + serverDirectory);
        return this.serverDirectory;
    }

    public String getServerDirectoryAsString() {
        if(this.serverDirectory == null)
            return "";
        return this.serverDirectory.toString();
    }

    public String getServerFilename() {
//        Cyberduck.DEBUG("[Bookmark] getServerFilename():return " + serverFilename);
        return this.serverFilename;
    }

    private void addHistoryPath(Path p) {
        pathHistory.add(p);
    }
    
    public Path getPreviousPath() {
        Cyberduck.DEBUG("Content of path history:"+pathHistory.toString());
        int size = pathHistory.size();
        if((size != -1) && (size > 1)) {
            Path p = (Path)pathHistory.get(size-2);
            //delete the fetched path - otherwise we produce a loop
            pathHistory.remove(size-1);
            pathHistory.remove(size-2);
            return p;
        }
        return this.getCurrentPath();
    }

    public String getPreviousPathAsString() {
        Path p;
        if((p = this.getPreviousPath()) != null)
            return this.getPreviousPath().toString();
        return "";
    }

    /**
     * Set all local pathnames.
     * @param File the absolute path pointing to the local filename
     */
    public void setLocalPath(File p) {
        Cyberduck.DEBUG("[Bookmark] setLocalPath("+p.toString() + ")");
        this.localDirectory = p.getParentFile();
        /*
        if(Preferences.instance().getProperty("files.encode").equals("true")) {
            this.localFilename = URLDecoder.decode(p.getName());
        }
        else {
            */
        this.localFilename = p.getName();
        this.localFinalPath = new File(localDirectory, localFilename);
        //@warning renaming files does not work on win32, don't use .partial
        if(System.getProperty("os.name").indexOf("Win") != -1)
            this.localTempPath = localFinalPath;
        else
            this.localTempPath = new File(localDirectory, localFilename + ".partial");
        this.callObservers(Bookmark.SELECTION);
        //this.callObservers(Bookmark.EDIT);
    }
    
    public String getLocalFilename() {
//        Cyberduck.DEBUG("[Bookmark] getLocalFilename():" + localFilename);
        return localFilename;
    }
    public File getLocalDirectory() {
//        Cyberduck.DEBUG("[Bookmark] getLocalDirectory():" + localDirectory);
        return localDirectory;
    }
    public File getLocalPath() {
//        Cyberduck.DEBUG("[Bookmark] getLocalPath():" + localFinalPath);
        return localFinalPath;
    }
    public String getLocalPathAsString() {
//        Cyberduck.DEBUG("[Bookmark] getLocalPathAsString():" + localFinalPath);
        if(localFinalPath == null)
            return "";
        return localFinalPath.toString();
    }
    public File getLocalTempPath() {
//        Cyberduck.DEBUG("[Bookmark] getLocalTempPath():" + localTempPath);
        return localTempPath;
    }

    /**
     * @return Either the filename or server directory string.
     */
    public String getDescription() {
        if(this.isDownload()) {
            return this.getServerFilename();
        }
        /*
        if(this.isUpload()) {
            return this.getLocalFilename();
        }
         */
        if(this.isListing()) {
            return this.getServerDirectoryAsString();
        }
        return "<unknown file or directory>";
    }

    /**
     * @return the appropriate connection for this transfer.
     */
    /*
    private Session getSession(Client client, TransferAction action) throws IOException {
        Cyberduck.DEBUG("[Bookmark] getSession()");
        if(this.getProtocol().equalsIgnoreCase(Session.HTTPS)) {
            Cyberduck.DEBUG("[Bookmark] getSession():return new HTTPSession()");
            return new HTTPSession(client, this, action, true);
        }
        if(this.getProtocol().equalsIgnoreCase(Session.HTTP)) {
            Cyberduck.DEBUG("[Bookmark] getSession():return new HTTPSession()");
            return new HTTPSession(client, this, action, false);
        }
        if(this.getProtocol().equalsIgnoreCase(Session.FTP)) {
            Cyberduck.DEBUG("[Bookmark] getSession():return new FTPSession()");
            return new FTPSession(client, this, action, false);
        }
        if(this.getProtocol().equalsIgnoreCase(Session.SFTP)) {
            Cyberduck.DEBUG("[Bookmark] getSession():return new SFTPSession()");
            return new SFTPSession(client, action, this, true);
        }
        throw new IOException("Cyberduck doesn't know the protocol '" + protocol + " '.");
    }
*/

    private Session getSession(TransferAction action) throws IOException {
        Cyberduck.DEBUG("[Bookmark] getSession()");
        if(this.getProtocol().equalsIgnoreCase(Session.HTTPS)) {
            Cyberduck.DEBUG("[Bookmark] getSession():return new HTTPSession()");
            return new HTTPSession(this, action, true);
        }
        if(this.getProtocol().equalsIgnoreCase(Session.HTTP)) {
            Cyberduck.DEBUG("[Bookmark] getSession():return new HTTPSession()");
            return new HTTPSession(this, action, false);
        }
        if(this.getProtocol().equalsIgnoreCase(Session.FTP)) {
            Cyberduck.DEBUG("[Bookmark] getSession():return new FTPSession()");
            return new FTPSession(this, action);
        }
        if(this.getProtocol().equalsIgnoreCase(Session.SFTP)) {
            Cyberduck.DEBUG("[Bookmark] getSession():return new SFTPSession()");
            return new SFTPSession(this, action);
        }
        throw new IOException("Cyberduck doesn't know the protocol '" + protocol + " '.");
    }
    
    /**
     * @return the appropriate client for this transfer.
     */
    /*
    private Client getClient() {
        if(this.getProtocol().equalsIgnoreCase(Session.HTTP) || this.getProtocol().equalsIgnoreCase(Session.HTTPS)) {
            if(this.client == null) {
                return new HTTPClient();
            }
            else {
                // if the protocol has changed we have to instantiate a new client.
                if(! (this.client instanceof HTTPClient)) {
                    return new HTTPClient();
                }
            }
            return this.client;
        }
        if(this.getProtocol().equalsIgnoreCase(Session.FTP)) {
            if(this.client == null) {
                return new FTPClient();
            }
            else {
                // if the protocol has changed we have to instantiate a new client.
                if(! (this.client instanceof FTPClient)) {
                    return new FTPClient();
                }
            }
            return this.client;
        }
        if(this.getProtocol().equalsIgnoreCase(Session.SFTP)) {
            if(this.client == null) {
		return new SFTPClient();
            }
            else {
                // if the protocol has changed we have to instantiate a new client.
                if(! (this.client instanceof SFTPClient)) {
                    return new SFTPClient();
                }
            }
	}
        throw new IllegalStateException("Can't find client for protocol " +this.getProtocol());
    }
    */

    /**
     * @return if this bookmark is a download.
     */
    public boolean isDownload() {
        if(this.getServerPath() == null || this.getLocalPath() == null) {
            return false;
        }
        return this.getServerPath().isFile();
    }

    /**
     * @return if this bookmark is a listing (only true if protocol is ftp).
     */
    public boolean isListing() {
//        if(!this.getProtocol().equals(Session.FTP)) {
//            return false;
//        }
        if(this.getServerPath() == null) {
            return false;
        }
        return this.getServerPath().isDirectory();
    }


    /**
    * First determines the type of action with <code>isDownload()</code> and <code>isListing()</code>
     * and then initiates the transfer with either <code>TransferAction.GET</code> or <code>TransferAction.LIST</code>.
     * If determination of transfer action fails, we beep.
     * @see #isDownload
     * @see #isListing
    */
    public void transfer() {
        if(this.isDownload()) {
            status.setPanelProperty(Status.PROGRESSPANEL);
            this.transfer(new TransferAction(TransferAction.GET));
            return;
        }
        if(this.isListing()) {
            status.setPanelProperty(Status.LISTPANEL);
            this.transfer(new TransferAction(TransferAction.LIST));
            return;
        }
        Cyberduck.beep();
    }

    /**
    * @param action The action describing what we have to do.
    */
    public void transfer(TransferAction action) {
        this.transfer(action, false);
    }

    /**
     * @param action The action describing what we have to do.
     * @param queued If the connection should be added to the queue instead
     * being executed immediatly. Use transfer.startQueue() to start the queue later.
     */
    public void transfer(TransferAction action, boolean queued) {
        if(session != null) {
            if(session.isAlive()) {
                Cyberduck.DEBUG("Session still alive.");
                Cyberduck.beep();
                return;
            }
        }
// cocoa-version        if(Preferences.instance().getProperty("interface.multiplewindow").equals("true")) {
// cocoa-version	(this.getStatusDialog()).show();
// cocoa-version        }
        try {
            if(action.toString().equals(TransferAction.GET)) {
                if(! (new Check(this, status).validate(this.getHandler())))
                   return;
            }
//            session = this.getSession(client = this.getClient(), action);
            session = this.getSession(action);
            if(queued) {
                if(queue == null) {
                    queue = new Queue();
                }
                queue.add(session);
                return;
            }
            session.start();
        }
        catch(IOException e) {
            status.setMessage("IO Error: " + e.getMessage(), Status.ERROR);
            status.fireStopEvent();
        }
    }

/*
    public void edit() {
        this.status.setPanelProperty(Status.EDITPANEL);
        if(Preferences.instance().getProperty("interface.multiplewindow").equals("true"))
            (this.getStatusDialog()).show();
    }
  */  
    /**
     * Start the connection queue.
     */
    public void startQueue() {
        Cyberduck.DEBUG("[Bookmark] startQueue()");
        this.queue.start();
        this.queue = null;
    }

    /**
     * Opens the local file with it's preferred application.
     * Does only work on mac os x, because of use of the 'open' command.
     */
    public void open() {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("open " + this.getLocalPath());
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param info User information in the format user:pass.
     */
    public void setUserInfo(String info) {
        Cyberduck.DEBUG("[Bookmark] setUserInfo(" + info + ")");
        if(info != null) {
            int i = info.indexOf(':');
            if(i != -1) {
                this.setUsername(info.substring(0, i));
                this.setPassword(info.substring(i + 1));
//                this.client = null;
            }
            else {
                this.setUsername(info);
            }
        }
    }

    private void setUsername(String u) {
        Cyberduck.DEBUG("[Bookmark] setUsername(" + username + ")");
    	this.username = u;
    }
    /**
    * @return The username to login with to the remote host
    */
    public String getUsername() {
//        Cyberduck.DEBUG("[Bookmark] getUsername():" + username);
    	return username;
    }
    
    private void setPassword(String p) {
        Cyberduck.DEBUG("[Bookmark] setPassword(" + passwd + ")");
    	this.passwd = p;
    }
    /**
    * @return The pass to login with to the remote host
    */
    public String getPassword() {
//        Cyberduck.DEBUG("[Bookmark] getPassword():" + passwd);
    	return passwd;
    }

    /**
     * @param p The port to use. If p < 0 the default ports are set.
     */
    public void setPort(int p) {
        Cyberduck.DEBUG("[Bookmark] setPort(" + p + ")");
        if(p < 0) {
            if(this.getProtocol().equalsIgnoreCase(Session.HTTP)) {
                this.port = Session.HTTP_PORT;
                return;
            }
            if(this.getProtocol().equalsIgnoreCase(Session.HTTPS)) {
                this.port = Session.HTTPS_PORT;
                return;
            }
            if(this.getProtocol().equalsIgnoreCase(Session.FTP)) {
                this.port = Session.FTP_PORT;
                return;
            }
            if(this.getProtocol().equalsIgnoreCase(Session.SFTP)) {
                this.port = Session.SFTP_PORT;
                return;
            }
        }
        this.port = p;
    }

    public int getPort() {
//	Cyberduck.DEBUG("[Bookmark] getPort():" + port);
        return this.port;
    }

    /**
    * Set the name of the remote host
     * @param h The host name
    */
    public void setHost(String h) {
        if(this.host != null) {
            if(!(this.host.equalsIgnoreCase(h))) {
                this.setUsername(Preferences.instance().getProperty("ftp.login.anonymous.name"));
                this.setPassword(Preferences.instance().getProperty("ftp.login.anonymous.pass"));
//                this.client = null;
            }
        }
        this.host = h;
    }

    /**
    * @return the host name or "Unknown host" if null
    */
    public String getHost() {
        if(host == null)
            return "Unknown host";
        return this.host;
    }

    /**
    * @return The IP address of the remote host if available
    */
    public String getIp() {
        //if we call getByName(null) InetAddress would return localhost
        if(host == null)
            return "Unknown host";
        try {
            return java.net.InetAddress.getByName(host).toString();
        }
        catch(java.net.UnknownHostException e) {
            return "Unknown host";
        }
    }

    /**
     * @param t If null, the default transfer type is set
     */
    public void setTransferType(FTPTransferType t) {
	/*
        if(t == null) {
            if(Preferences.instance().getProperty("connection.transfertype.default").equalsIgnoreCase("ascii"))
                this.transfertype = FTPTransferType.ASCII;
            else
                this.transfertype = FTPTransferType.BINARY;
        }
        else
	 */
	this.transfertype = t;
    }

    /**
    */
    public FTPTransferType getTransferType() {
        return this.transfertype;
    }

    /**
     * @return The preferred download handler. (intial, resume, reload)
     * @see Message
     */
    public Message getHandler() {
        if(status.isComplete()) {
            return Status.RELOAD;
        }
        else if(status.isStopped()) {
            if(status.getCurrent() <= 0) {
                return Status.INITIAL;
            }
            else {
                return Status.RESUME;
            }
        }
        return Status.RESUME;
    }

    /**
     * @return The current directory listing. (ftp only)
     */
    public List getListing()  {
        return this.listing;
    }

    /**
     * @param listing The directory list
     */
    public void setListing(List listing) {
        Cyberduck.DEBUG("[Bookmark] setListing(" + listing + ")");
        this.listing = listing;
        this.callObservers(Bookmark.LIST);
    }

    /**
    * @return true if a protocol, host and serverpath aren't null
    */
    public boolean isValid() {
        return this.getProtocol() != null && this.getHost() != null && this.getServerPath() != null;
    }


    /**
    * close all dialogs related to this bookmark and close any open connnections.
    */
    public void cleanup() {
// cocoa-version        if(statusDialog != null) {
// cocoa-version            statusDialog.dispose();
// cocoa-version        }
//        if(client != null) {
//            if(client.isAlive()) {
//                try {
//                    client.quit();
//                }
//                catch(java.io.IOException io) {
//                    Cyberduck.DEBUG("[Bookmark] cleanup():"+io.getMessage());
//                }
//                catch(ch.cyberduck.connection.SessionException se) {
//                    Cyberduck.DEBUG("[Bookmark] cleanup():"+se.getMessage());
//                }
//            }
//        }
        //notify observers to delete references to this bookmark
        this.callObservers(Bookmark.DESELECTION);
        //Clears the observer list so that this object no longer has any observers.
        this.deleteObservers();
        this.status.deleteObservers();
    }

    /**
    * @return the address (url) as a string
     * @see #getAddressAsString
    */
    public String toString() {
        return this.getAddressAsString();
    }

    /**
    * Build a status dialog which gets notified by this (and only this) bookmark
    * @return A status dialog initialized with the properties of this bookmark
     * @see ch.cyberduck.ui.StatusPanel
     * @see ch.cyberduck.ui.common.DefaultFrame
    */
    /*
    public javax.swing.JDialog getStatusDialog() {
        if(this.statusDialog == null) {
            this.statusDialog =  new ch.cyberduck.ui.common.DefaultFrame(this.getHost(), true);
            ch.cyberduck.ui.StatusPanel statusPanel = new ch.cyberduck.ui.StatusPanel(this);
            statusPanel.setParent(statusDialog);
            statusDialog.add(statusPanel);
//            statusDialog.setSize(content.getPreferredSize());
            statusDialog.pack();
        }
        return this.statusDialog;
    }
     */
}
