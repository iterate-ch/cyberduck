package ch.cyberduck.connection.ftp;

/*
 *  ch.cyberduck.connection.ftp.FTPSession.java
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

import com.enterprisedt.net.ftp.*;

import ch.cyberduck.Cyberduck;
import ch.cyberduck.Preferences;
import ch.cyberduck.connection.*;

/**
 * Opens a connection to the remote server via ftp protocol
 * @version $Id$
 */
public class FTPSession extends Session {

    private FTPClient FTP;

    /**
     * @param client The client to use which does implement the ftp protocol
     * @param action The <code>TransferAction</code> to execute after the connection has been opened
     * @param transfer The <code>Bookmark</code> object
     * @param secure If the connection is secure
     */
    public FTPSession(Bookmark b, TransferAction action) {
        super(b, action);
	this.FTP = new FTPClient();
    }

    private void connect() throws IOException, FTPException {
	this.log("Connecting to " + bookmark.getHost(), Status.PROGRESS);
	this.log("\nConnecting to " + bookmark.getIp()+"\n", Status.TRANSCRIPT);
	if(Preferences.instance().getProperty("ftp.active").equals("true")) {
	    FTP.setConnectMode(FTPConnectMode.ACTIVE);
	}
	else if(Preferences.instance().getProperty("ftp.passive").equals("true")) {
	    FTP.setConnectMode(FTPConnectMode.PASV);
	}
	if(Preferences.instance().getProperty("connection.proxy").equals("true")) {
	    FTP.initSOCKS(Preferences.instance().getProperty("connection.proxy.port"), Preferences.instance().getProperty("connection.proxy.host"));
	}
	if(Preferences.instance().getProperty("connection.proxy.authenticate").equals("true")) {
	    FTP.initSOCKSAuthentication(Preferences.instance().getProperty("connection.proxy.username"), Preferences.instance().getProperty("connection.proxy.password"));
	}
	FTP.connect(bookmark.getHost(), bookmark.getPort());
	FTP.setTimeout(Integer.parseInt(Preferences.instance().getProperty("connection.timeout"))*60*1000);
	this.log("Unsecure connection established.", Status.PROGRESS);
	try {
	    this.log("Authenticating as " + bookmark.getUsername() + "...", Status.PROGRESS);
	    FTP.login(bookmark.getUsername(), bookmark.getPassword());
	    this.log("Login successfull.", Status.PROGRESS);
	}
	catch(FTPException e) {
	    bookmark.status.setPanelProperty(Status.LOGINPANEL);
	    throw e;
	}
	FTP.system();
    }
    
    private void list(Path directory) throws FTPException, IOException {
        FTP.setType(FTPTransferType.ASCII);
	Path currentDirectory;
        if(directory.getPath().equals("/") && bookmark.getCurrentPath().getPath().equals("/")) {
            String cwd = FTP.pwd();
//            cwd = cwd.substring(cwd.indexOf('"') + 1, cwd.lastIndexOf('"'));
            Path current = null;
            if(cwd.length() == 1 && cwd.charAt(0) == '/')
                current = new Path(cwd);
            else
                current = new Path(cwd +"/");
            bookmark.setCurrentPath(current);
        }
        else {
            bookmark.setCurrentPath(directory);
            FTP.chdir(directory.getPath());
        }
        bookmark.setListing(FTPParser.parseList(bookmark.getCurrentPath().getPath(), FTP.dir()));
    }

    private void delete(Path path) throws SessionException, IOException {
        if(path.isDirectory()) {
            FTP.chdir(path.getName());
            java.util.List files = FTPParser.parseList(path.getPath(), FTP.dir());
            java.util.Iterator iterator = files.iterator();
            Path file = null;
            while(iterator.hasNext()) {
                file = (Path)iterator.next();
                if(file.isDirectory()) {
                    this.delete(file);
                }
                if(file.isFile()) {
                    this.deleteFile(file.getName());
                }
            }
            FTP.cdup();
            FTP.rmdir(path.getName());
        }
        if(path.isFile()) {
            this.deleteFile(path.getName());
        }
    }

    private void deleteFile(String file) throws SessionException, IOException {
        this.check();
        this.log("Deleting '" + file + "'...", Status.PROGRESS);
        FTP.delete(file);
    }

    private void putDirectory(java.io.File file) throws SessionException, IOException {
        FTP.mkdir(file.getName());
        FTP.chdir(file.getName());
        File[] files = file.listFiles();
        for(int i = 0; i < files.length; i++) {
            if(files[i].isDirectory()) {
                this.putDirectory(files[i]);
            }
            if(files[i].isFile()) {
                this.putFile(files[i]);
            }
        }
        FTP.cdup();
    }

    private void putFile(File file) throws SessionException, IOException {
        this.check();
        bookmark.status.fireActiveEvent();
        bookmark.setLocalPath(file);
        bookmark.status.setLength((int)file.length());
        bookmark.status.setCurrent(0);
        if(bookmark.getTransferType().equals(FTPTransferType.BINARY)) {
            //this.log("Setting transfer mode to BINARY.", Message.PROGRESS);
            FTP.setType(FTPTransferType.BINARY);

            java.io.InputStream in = new FileInputStream(file);
            if(in == null) {
                throw new IOException("Unable to buffer data");
            }

            this.log("Opening data stream...", Status.PROGRESS);

            java.io.OutputStream out = FTP.putBinary(file.getName(), bookmark.status.isResume());
            if(out == null) {
                throw new IOException("Unable opening data stream");
            }
            this.log("Uploading "+file.getName()+"...", Status.PROGRESS);
            this.upload(out, in);
	    FTP.validateTransfer();
        }
        else if(bookmark.getTransferType().equals(FTPTransferType.ASCII)) {
            //this.log("Setting transfer type to ASCII.", Message.PROGRESS);
            FTP.setType(FTPTransferType.ASCII);

            java.io.Reader in = new FileReader(file);
            if(in == null) {
                throw new IOException("Unable to buffer data");
            }

            this.log("Opening data stream...", Status.PROGRESS);
            java.io.Writer out = FTP.putASCII(file.getName(), bookmark.status.isResume());
            if(out == null) {
                throw new IOException("Unable opening data stream");
            }
            this.log("Uploading "+file.getName()+"...", Status.PROGRESS);
            this.upload(out, in);
	    FTP.validateTransfer();
        }
    }

    /**
     * @param remote The absolute path of the file on the server
     * @param local The name of the file to save locally
     */
    private void getDirectory(Path remote, File local) throws SessionException, IOException {
        java.util.List files = FTPParser.parseList(remote.getPath(), FTP.dir());
        File dir = new File(local, remote.getName());
        //Cyberduck.DEBUG("making directory: "+dir.toString());
        dir.mkdir();
        java.util.Iterator i = files.iterator();
        while(i.hasNext()) {
            Path r = (Path)i.next();
            if(r.isDirectory()) {
                //Cyberduck.DEBUG("changing directory: "+r.toString());
                FTP.chdir(r.getPath());
                this.getDirectory(r, dir);
            }
            if(r.isFile()) {
                bookmark.setServerPath(r.getPath());
                bookmark.setLocalPath(new java.io.File(dir, r.getName()));
                bookmark.status.setCurrent(0);
                //Cyberduck.DEBUG("getting file:"+r.toString());
                this.getFile(r.getName(), bookmark.getLocalTempPath());
            }
        }
        //Cyberduck.DEBUG("upping directory");
        FTP.cdup();
    }

    /**
     * @param file The filename of the remote file
     * @param local The absolute path where to store the file locally
     */
    private void getFile(String file, File local) throws SessionException, IOException {
        this.check();
        bookmark.status.fireActiveEvent();
        if(bookmark.getTransferType().equals(FTPTransferType.BINARY)) {
            //this.log("Setting transfer mode to BINARY", Message.PROGRESS);
            FTP.setType(FTPTransferType.BINARY);
            bookmark.status.setLength((int)(FTP.size(file)));
            OutputStream out = new FileOutputStream(local.toString(), bookmark.status.isResume());
            if(out == null) {
                throw new IOException("Unable to buffer data");
            }
            this.log("Opening data stream...", Status.PROGRESS);
//	    public void get(OutputStream destStream, String remoteFile)
            java.io.InputStream in = FTP.getBinary(file, bookmark.status.isResume() ? bookmark.status.getCurrent() : 0);
            if(in == null) {
                throw new IOException("Unable opening data stream");
            }
            this.log("Downloading "+file+"...", Status.PROGRESS);
            this.download(in, out);
           FTP.validateTransfer();
        }
        else if(bookmark.getTransferType().equals(FTPTransferType.ASCII)) {
            //this.log("Setting transfer type to ASCII", Message.PROGRESS);
            FTP.setType(FTPTransferType.ASCII);
            bookmark.status.setLength((int)(FTP.size(file)));
            java.io.Writer out = new FileWriter(local.toString(), bookmark.status.isResume());
            if(out == null) {
                throw new IOException("Unable to buffer data");
            }
            this.log("Opening data stream...", Status.PROGRESS);
            java.io.Reader in = FTP.getASCII(file, bookmark.status.isResume() ? bookmark.status.getCurrent() : 0);
            if(in == null) {
                throw new IOException("Unable opening data stream");
            }
            this.log("Downloading "+file+"...", Status.PROGRESS);
            this.download(in, out);
            FTP.validateTransfer();
        }
        else {
            throw new FTPException("Unknown transfer type");
        }
    }

    public void run() {
        bookmark.status.fireActiveEvent();
        Cyberduck.DEBUG("[FtpSession] run():" + action.toString() + "************************************");
        try {
            this.log("Checking status...", Status.PROGRESS);
            if(action.toString().equals(TransferAction.QUIT)) {
                if(FTP.isAlive()) {
	                this.log("Disconnecting from '" + bookmark.getHost() + "'...", Status.PROGRESS);
                    FTP.quit();
                }
                this.log("Disconnected", Status.PROGRESS);
                return;
            }
            if(!FTP.isAlive()) {
                this.connect();
            }
            try {
                FTP.noop();
            }
            catch(IOException e) {
                Cyberduck.DEBUG(e.getMessage());
                this.connect();
            }
            catch(SessionException e) {
                Cyberduck.DEBUG(e.getMessage());
                this.connect();
            }
            this.check();
            if(action.toString().equals(TransferAction.GET)) {
                FTP.chdir(bookmark.getServerDirectoryAsString());
                Path p = bookmark.getServerPath();
                if(p.isFile()) {
                    this.getFile(bookmark.getServerFilename(), bookmark.getLocalTempPath());
                }
                if(p.isDirectory()) {
                    bookmark.status.ignoreEvents(true);
                    this.getDirectory(p, new java.io.File(Preferences.instance().getProperty("download.path")));
                    bookmark.status.ignoreEvents(false);
                    bookmark.status.fireCompleteEvent();
                }
                FTP.quit();
            }
            else if(action.toString().equals(TransferAction.PUT)) {
                FTP.chdir(bookmark.getCurrentPathAsString());
                File f = (File)action.getParam();
                if(f.isDirectory()) {
                    bookmark.status.ignoreEvents(true);
                    this.putDirectory(f);
                    bookmark.status.ignoreEvents(false);
                    bookmark.status.fireCompleteEvent();
                }
                if(f.isFile()) {
                    this.putFile(f);
                }
                this.list(bookmark.getCurrentPath());
            }
            else if(action.toString().equals(TransferAction.LIST)) {
                Path directory;
                if(action.getParam() == null)
                    directory = bookmark.getCurrentPath();
                else
                    directory = (Path)action.getParam();
                this.log("Listing directory '" + directory + "'...", Status.PROGRESS);
                this.list(directory);
                //this.log("Listing of '" + directory + "' complete", Message.PROGRESS);
            }
            else if(action.toString().equals(TransferAction.MKDIR)) {
                FTP.chdir(bookmark.getCurrentPathAsString());
                this.log("Making directory '" + action.getParam() + "'...", Status.PROGRESS);
                FTP.mkdir((String)action.getParam());
                this.list(bookmark.getCurrentPath());
            }
            else if(action.toString().equals(TransferAction.DELE)) {
                FTP.chdir(bookmark.getCurrentPathAsString());
                this.log("Deleting '" + action.getParam() + "'...", Status.PROGRESS);
                this.delete((Path)action.getParam());
                //this.log("'" + action.getParam() + "' deleted", Message.PROGRESS);
                this.list(bookmark.getCurrentPath());
            }
            else if(action.toString().equals(TransferAction.RNFR)) {
                FTP.chdir(bookmark.getCurrentPathAsString());
                Path from = (Path)action.getParam();
                Path to = ((Path)action.getParam2()).getRelativePath(bookmark.getCurrentPath());
                this.log("Renaming '" + from + "' to '" + to + "'...", Status.PROGRESS);
                FTP.rename(from.getName(), to.getPath());
                this.list(bookmark.getCurrentPath());
            }
            else if(action.toString().equals(TransferAction.SITE)) {
                String command = (String)action.getParam();
                this.log("Running SITE command...", Status.PROGRESS);
                FTP.site(command);
                this.list(bookmark.getCurrentPath());
                //this.log("Renaming '" + from.getName() + "' to '" + to.getName() + "'...", Status.PROGRESS);
            }
            else {
                throw new FTPException("Unknown action: " + action.toString());
            }
            this.log("Command completed.", Status.PROGRESS);
        }
        catch (SessionException e) {
            this.log("FTP Error: " + e.getReplyCode() + " " + e.getMessage(), Status.ERROR);
            this.log("Incomplete", Status.PROGRESS);
        }
        catch (IOException e) {
            this.log("IO Error: " + e.getMessage(), Status.ERROR);
            this.log("Incomplete", Status.PROGRESS);
        }
        finally {
            this.saveLog();
            this.bookmark.status.ignoreEvents(false);
            this.bookmark.status.fireStopEvent();
        }
    }
}
