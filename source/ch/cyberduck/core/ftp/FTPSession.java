package ch.cyberduck.core.ftp;

/*
 *  ch.cyberduck.core.ftp.FTPSession.java
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
import java.util.*;

import com.enterprisedt.net.ftp.*;

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.*;

import org.apache.log4j.Logger;

/**
 * Opens a connection to the remote server via ftp protocol
 * @version $Id$
 */
public class FTPSession extends Session {

    private static Logger log = Logger.getLogger(Session.class);

    class FTPFile extends Path {
	public FTPFile(String parent, String name) {
	    super(parent, name);
	}

	public FTPFile(String path) {
	    super(path);
	}

	public Path getParent() {
	    String abs = this.getAbsolute();
	    if((parent == null) && !abs.equals("/")) {
		int index = abs.lastIndexOf('/');
		String dirname = abs;
		if(index > 0) {
		    dirname = abs.substring(0, index);
		}
		Path file = new FTPFile(dirname);
	    }
	    return parent;
	}
	
	public List list() {
	    log.debug("list");
	    List files = null;
	    try {
		FTP.setType(FTPTransferType.ASCII);
//		Path currentDirectory;
//		if(directory.getAbsolute().equals("/") && bookmark.getCurrentPath().getAbsolute().equals("/")) {
//		    String cwd = FTP.pwd();
//            cwd = cwd.substring(cwd.indexOf('"') + 1, cwd.lastIndexOf('"'));
//		    Path current = null;
//		    if(cwd.length() == 1 && cwd.charAt(0) == '/')
//			current = new FTPFile(cwd);
//		    else
//			current = new Path(cwd +"/");
//		    bookmark.setCurrentPath(current);
//		}
//		else {
//		    bookmark.setCurrentPath(directory);
//		    FTP.chdir(directory.getAbsolute());
//		}
		FTP.chdir(this.getAbsolute());
		files = new FTPParser().parseList(this.getAbsolute(), FTP.dir());
                host.callObservers(this);
		host.callObservers(files);
	    }
	    catch(FTPException e) {
		FTPSession.this.log(e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		FTPSession.this.log(e.getMessage(), Message.ERROR);
	    }
	    return files;
	}

	public void delete() {
	    log.debug("delete");
	    try {
		if(this.isDirectory()) {
		    List files = this.list();
		    java.util.Iterator iterator = files.iterator();
		    Path file = null;
		    while(iterator.hasNext()) {
			file = (Path)iterator.next();
			if(file.isDirectory()) {
			    file.delete();
			}
			if(file.isFile()) {
			    FTP.delete(file.getName());
			}
		    }
		    FTP.cdup();
		    FTP.rmdir(this.getName());
		}
		if(this.isFile()) {
		    FTP.delete(this.getName());
		}
	    }
	    catch(FTPException e) {
		FTPSession.this.log(e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		FTPSession.this.log(e.getMessage(), Message.ERROR);
	    }
	}

        public void rename(String filename) {
            log.debug("rename");
            try {
                FTP.chdir(this.getParent().getAbsolute());
                this.log("Renaming '" + this.getName() + "' to '" + filename + "'...", Message.PROGRESS);
                FTP.rename(this.getName(), filename);
                this.getParent().list();
                catch(FTPException e) {
                    FTPSession.this.log(e.getMessage(), Message.ERROR);
                }
                catch(IOException e) {
                    FTPSession.this.log(e.getMessage(), Message.ERROR);
                }
            }
        }

        public void mkdir() {
            log.debug("mkdir");
            try {
                FTP.mkdir(this.getName());
            }
            catch(FTPException e) {
                FTPSession.this.log(e.getMessage(), Message.ERROR);
            }
            catch(IOException e) {
		FTPSession.this.log(e.getMessage(), Message.ERROR);
	    }
	}
    }
    
    
    private FTPClient FTP;

    /**
     * @param client The client to use which does implement the ftp protocol
     * @param action The <code>TransferAction</code> to execute after the connection has been opened
     * @param transfer The <code>Bookmark</code> object
     * @param secure If the connection is secure
     */
    public FTPSession(Host h) {//, TransferAction action) {
        super(h);
	this.FTP = new FTPClient();
    }

    public void close() {
	try {
	    if(FTP != null) {
		this.log("Disconnecting...", Message.PROGRESS);
		FTP.quit();
	    }
	    this.log("Disconnected", Message.PROGRESS);
	}
	catch(FTPException e) {
	    e.printStackTrace();
	}
	catch(IOException e) {
	    e.printStackTrace();
	}
	host.status.fireStopEvent();
    }

    public void run() {
	//            if(!FTP.isAlive()) {
	try {
	    this.log("Opening FTP connection to " + host.getIp()+"...", Message.PROGRESS);

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

	    FTP.connect(host.getName(), host.getPort());
	    FTP.setTimeout(Integer.parseInt(Preferences.instance().getProperty("connection.timeout"))*60*1000);
	    this.login();
	    FTP.system();
            String path = host.getPath().equals(Preferences.instance().getProperty("connection.path.default")) ? FTP.pwd() : host.getPath();
	    FTPFile home = new FTPFile(path);
	    home.list();
	}
	catch(FTPException e) {
            this.log(e.getMessage(), Message.ERROR);
	    //            this.log("Incomplete", Message.PROGRESS);
	    this.close();
	}
	catch(IOException e) {
            this.log(e.getMessage(), Message.ERROR);
	    //            this.log("Incomplete", Message.PROGRESS);
	    this.close();
	}
    }

    private void login() throws IOException {
	try {
	    this.log("Authenticating as " + host.login.getUsername() + "...", Message.PROGRESS);
	    FTP.login(host.login.getUsername(), host.login.getPassword());
	    this.log("Login successfull.", Message.PROGRESS);
	}
	catch(FTPException e) {
            if(host.getLogin().loginFailure()) {
                // let's try again with the new values
		this.login();
            }
	    else {
		this.log(e.getMessage(), Message.ERROR);
		this.close();
	    }
	}
    }


    
    /*
    private void list(Path directory) throws FTPException, IOException {
        FTP.setType(FTPTransferType.ASCII);
	Path currentDirectory;
        if(directory.getAbsolute().equals("/") && bookmark.getCurrentPath().getAbsolute().equals("/")) {
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
            FTP.chdir(directory.getAbsolute());
        }
        bookmark.setListing(FTPParser.parseList(bookmark.getCurrentPath().getAbsolute(), FTP.dir()));
    }

    private void delete(Path path) throws SessionException, IOException {
        if(path.isDirectory()) {
            FTP.chdir(path.getName());
            java.util.List files = FTPParser.parseList(path.getAbsolute(), FTP.dir());
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
        this.log("Deleting '" + file + "'...", Message.PROGRESS);
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

            this.log("Opening data stream...", Message.PROGRESS);

            java.io.OutputStream out = FTP.putBinary(file.getName(), bookmark.status.isResume());
            if(out == null) {
                throw new IOException("Unable opening data stream");
            }
            this.log("Uploading "+file.getName()+"...", Message.PROGRESS);
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

            this.log("Opening data stream...", Message.PROGRESS);
            java.io.Writer out = FTP.putASCII(file.getName(), bookmark.status.isResume());
            if(out == null) {
                throw new IOException("Unable opening data stream");
            }
            this.log("Uploading "+file.getName()+"...", Message.PROGRESS);
            this.upload(out, in);
	    FTP.validateTransfer();
        }
    }

     */

    /**
     * @param remote The absolute path of the file on the server
     * @param local The name of the file to save locally
     */

    /*
    private void getDirectory(Path remote, File local) throws SessionException, IOException {
        java.util.List files = FTPParser.parseList(remote.getAbsolute(), FTP.dir());
        File dir = new File(local, remote.getName());
        //log.debug("making directory: "+dir.toString());
        dir.mkdir();
        java.util.Iterator i = files.iterator();
        while(i.hasNext()) {
            Path r = (Path)i.next();
            if(r.isDirectory()) {
                //log.debug("changing directory: "+r.toString());
                FTP.chdir(r.getAbsolute());
                this.getDirectory(r, dir);
            }
            if(r.isFile()) {
                bookmark.setServerPath(r.getAbsolute());
                bookmark.setLocalPath(new java.io.File(dir, r.getName()));
                bookmark.status.setCurrent(0);
                //log.debug("getting file:"+r.toString());
                this.getFile(r.getName(), bookmark.getLocalTempPath());
            }
        }
        //log.debug("upping directory");
        FTP.cdup();
    }

     */

    /**
     * @param file The filename of the remote file
     * @param local The absolute path where to store the file locally
     */

    /*
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
            this.log("Opening data stream...", Message.PROGRESS);
//	    public void get(OutputStream destStream, String remoteFile)
            java.io.InputStream in = FTP.getBinary(file, bookmark.status.isResume() ? bookmark.status.getCurrent() : 0);
            if(in == null) {
                throw new IOException("Unable opening data stream");
            }
            this.log("Downloading "+file+"...", Message.PROGRESS);
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
            this.log("Opening data stream...", Message.PROGRESS);
            java.io.Reader in = FTP.getASCII(file, bookmark.status.isResume() ? bookmark.status.getCurrent() : 0);
            if(in == null) {
                throw new IOException("Unable opening data stream");
            }
            this.log("Downloading "+file+"...", Message.PROGRESS);
            this.download(in, out);
            FTP.validateTransfer();
        }
        else {
            throw new FTPException("Unknown transfer type");
        }
    }
     */

    /*
    public void run() {
        bookmark.status.fireActiveEvent();
        log.debug("[FtpSession] run():" + action.toString() + "************************************");
        try {
            this.log("Checking status...", Message.PROGRESS);
            if(action.toString().equals(TransferAction.QUIT)) {
                if(FTP.isAlive()) {
	                this.log("Disconnecting from '" + bookmark.getHost() + "'...", Message.PROGRESS);
                    FTP.quit();
                }
                this.log("Disconnected", Message.PROGRESS);
                return;
            }
            if(!FTP.isAlive()) {
                this.connect();
            }
            try {
                FTP.noop();
            }
            catch(IOException e) {
                log.debug(e.getMessage());
                this.connect();
            }
            catch(SessionException e) {
                log.debug(e.getMessage());
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
                this.log("Listing directory '" + directory + "'...", Message.PROGRESS);
                this.list(directory);
                //this.log("Listing of '" + directory + "' complete", Message.PROGRESS);
            }
            else if(action.toString().equals(TransferAction.MKDIR)) {
                FTP.chdir(bookmark.getCurrentPathAsString());
                this.log("Making directory '" + action.getParam() + "'...", Message.PROGRESS);
                FTP.mkdir((String)action.getParam());
                this.list(bookmark.getCurrentPath());
            }
            else if(action.toString().equals(TransferAction.DELE)) {
                FTP.chdir(bookmark.getCurrentPathAsString());
                this.log("Deleting '" + action.getParam() + "'...", Message.PROGRESS);
                this.delete((Path)action.getParam());
                //this.log("'" + action.getParam() + "' deleted", Message.PROGRESS);
                this.list(bookmark.getCurrentPath());
            }
            else if(action.toString().equals(TransferAction.RNFR)) {
                FTP.chdir(bookmark.getCurrentPathAsString());
                Path from = (Path)action.getParam();
                Path to = ((Path)action.getParam2()).getRelativePath(bookmark.getCurrentPath());
                this.log("Renaming '" + from + "' to '" + to + "'...", Message.PROGRESS);
                FTP.rename(from.getName(), to.getAbsolute());
                this.list(bookmark.getCurrentPath());
            }
            else if(action.toString().equals(TransferAction.SITE)) {
                String command = (String)action.getParam();
                this.log("Running SITE command...", Message.PROGRESS);
                FTP.site(command);
                this.list(bookmark.getCurrentPath());
                //this.log("Renaming '" + from.getName() + "' to '" + to.getName() + "'...", Message.PROGRESS);
            }
            else {
                throw new FTPException("Unknown action: " + action.toString());
            }
            this.log("Command completed.", Message.PROGRESS);
        }
        catch (SessionException e) {
            this.log("FTP Error: " + e.getReplyCode() + " " + e.getMessage(), Message.ERROR);
            this.log("Incomplete", Message.PROGRESS);
        }
        catch (IOException e) {
            this.log("IO Error: " + e.getMessage(), Message.ERROR);
            this.log("Incomplete", Message.PROGRESS);
        }
        finally {
            this.saveLog();
            this.bookmark.status.ignoreEvents(false);
            this.bookmark.status.fireStopEvent();
        }
    }

     */

    class FTPParser {
	private final String months[] = {
	    "JAN", "FEB", "MAR",
	    "APR", "MAY", "JUN",
	    "JUL", "AUG", "SEP",
	    "OCT", "NOV", "DEC"
	};

	public List parseList(String parent, String[] list) throws FTPException {
	    //        log.debug("[FTPParser] parseList(" + parent + "," + list + ")");
	    List parsedList = new ArrayList();
	    boolean showHidden = Preferences.instance().getProperty("ftp.showHidden").equals("true");
	    for(int i = 0; i < list.length; i++) {
		int index = 0;
		String line = list[i].trim();
		if(isValidLine(line)) {
		    Path p = parseListLine(parent, line);
		    String filename = p.getName();
		    if(!(filename.equals(".") || filename.equals(".."))) {
			if(!showHidden) {
			    if(filename.charAt(0) == '.') {
				p.setVisible(false);
			    }
			}
			parsedList.add(p);
		    }
		}
	    }
	    return parsedList;
	}


	/**
	    If the file name is a link, it may include a pointer to the original, in which case it is in the form "name -> link"
	 */
	public String parseLink(String link) {
	    if(!isValidLink(link)) {
		return null;
	    }
	    return link.substring(jumpWhiteSpace(link, link.indexOf("->")) + 3).trim();
	}

	public boolean isFile(String c) {
	    return c.charAt(0) == '-';
	}

	public boolean isLink(String c) {
	    return c.charAt(0) == 'l';
	}

	public boolean isDirectory(String c) {
	    //        log.debug("[FTPParser] isDirectory(" + c + ")");
	    return c.charAt(0) == 'd';
	}

	private Path parseListLine(String parent, String line) throws FTPException {
	    //        log.debug("[FTPParser] parseListLine("+ parent+","+line+")");
     // unix list format never strarts with number
	    if("0123456789".indexOf(line.charAt(0)) < 0) {
		return parseUnixListLine(parent, line);
	    }
	    // windows list format always starts with number
	    else {
		return parseWinListLine(parent, line);
	    }
	}


	private Path parseWinListLine(String path, String line) throws FTPException {
	    //        log.debug("[FTPParser] parseWinListLine("+ path+","+line+")");

	    // 10-16-01  11:35PM                 1479 file
     // 10-16-01  11:37PM       <DIR>          awt  *
	    Path p = null;
	    try {
		StringTokenizer toker = new StringTokenizer(line);
		long date = parseWinListDate (toker.nextToken(),  toker.nextToken());// time
		    String size2dir  = toker.nextToken();  // size or dir
		    String access;
		    int size = 0;
		    if(size2dir.equals("<DIR>")) {
			access = "d?????????";
		    }
		    else {
			access = "-?????????";
		    }
		    String name = toker.nextToken("").trim();
		    String owner = "";
		    String group = "";

		    if(isDirectory(access) && !(name.charAt(name.length()-1) == '/')) {
			name = name + "/";
		    }
		    p = new FTPFile(path, name);
		    p.setOwner(owner);
		    p.setModified(date);
		    p.setMode(access);
		    p.setPermission(new Permission(access));
		    p.setSize(size);
		    return p;
	    }
	    catch(NumberFormatException e) {
		throw new FTPException("Invalid server response : "+e.getMessage());
	    }
	    catch(StringIndexOutOfBoundsException e) {
		throw new FTPException("Invalid server response : "+e.getMessage());
	    }
	}

	private long parseWinListDate(String date, String time) throws NumberFormatException {
	    //10-16-01    11:35PM
     //10-16-2001  11:35PM
	    Calendar c = Calendar.getInstance();
	    StringTokenizer toker = new StringTokenizer(date,"-");
	    int m = Integer.parseInt(toker.nextToken()),
		d = Integer.parseInt(toker.nextToken()),
		y = Integer.parseInt(toker.nextToken());
	    if(y >= 70) y += 1900; else y += 2000;
	    toker = new StringTokenizer(time,":APM");
	    c.set(y,m,d,(time.endsWith("PM")?12:0)+
	   Integer.parseInt(toker.nextToken()),
	   Integer.parseInt(toker.nextToken()));
	    return c.getTime().getTime();
	}

	private Path parseUnixListLine(String path, String line) throws FTPException{
	    //        log.debug("[FTPParser] parseUnixListLine("+ path+","+line+")");

	    //drwxr-xr-x  33 root     wheel       1078 Mar 15 16:18 bin
     //lrwxrwxr-t   1 root     admin         13 Mar 16 13:38 cores -> private/cores
     //dr-xr-xr-x   2 root     wheel        512 Mar 16 02:38 dev
     //lrwxrwxr-t   1 root     admin         11 Mar 16 13:38 etc -> private/etc
     //lrwxrwxr-t   1 root     admin          9 Mar 16 13:38 mach -> /mach.sym
     //-r--r--r--   1 root     admin     563812 Mar 16 02:38 mach.sym
     //-rw-r--r--   1 root     wheel    3156580 Jan 25 07:06 mach_kernel
     //drwxr-xr-x   7 root     wheel        264 Jul 10  2001 private
     //drwxr-xr-x  59 root     wheel       1962 Mar 15 16:18 sbin
     //lrwxrwxr-t   1 root     admin         11 Mar 16 13:38 tmp -> private/tmp
     //drwxr-xr-x  11 root     wheel        330 Jan 31 08:15 usr
     //lrwxrwxr-t   1 root     admin         11 Mar 16 13:38 var -> private/var

	    Path p = null;
	    try {
		String link = null;
		if(isLink(line)) {
		    link = parseLink(line);
		    line = line.substring(0, line.indexOf("->")).trim();
		}
		StringTokenizer toker = new StringTokenizer(line);
		String access = toker.nextToken();  // access
		toker.nextToken();  // links
		String owner = toker.nextToken();  // owner
		String group = toker.nextToken();  // group
		String size = toker.nextToken();  // size
		if(size.endsWith(","))
		    size = size.substring(0,size.indexOf(","));
		String uu = size;
		if(access.startsWith("c"))
		    uu = toker.nextToken();             // device
					  // if uu.charAt(0) is not digit try uu_file format
		if("0123456789".indexOf(uu.charAt(0)) < 0) {
		    size = group;
		    group = "";
		}
		long date = parseUnixListDate(("0123456789".indexOf(uu.charAt(0)) < 0 ?uu
									:toker.nextToken()), // month
				toker.nextToken(),  // day
				toker.nextToken()); // time or year
		String name = toker.nextToken("").trim(); // name

		/*
		 //@ verify
		 if(FTPParser.isDirectory(access) && !(name.charAt(name.length()-1) == '/')) {
		     name = name + "/";
		 }
		 */

		p = new FTPFile(path, name);
		p.setOwner(owner);
		p.setGroup(group);
		p.setModified(date);
		p.setMode(access);
		p.setPermission(new Permission(access));
		p.setSize(Integer.parseInt(size));


		// @todo implement this in Path.class
		/*
		 if(isLink(line)) {
		     // the following lines are the most ugly. I just don't know how I can be sure
       // a link is a directory or a file. Now I look if there's a '.' and if we have execute rights.

		     //good chance it is a directory if everyone can execute
		     boolean execute = p.getPermission().getOtherPermissions()[Permission.EXECUTE];
		     boolean point = false;
		     if(link.length() >= 4) {
			 if(link.charAt(link.length()-3) == '.' || link.charAt(link.length()-4) == '.')
			     point = true;
		     }
		     boolean directory = false;
		     if(!point && execute)
			 directory = true;

		     if(directory) {
			 //log.debug("***Parsing link as directory:"+link);
			 if(!(link.charAt(link.length()-1) == '/'))
			     link = link+"/";
			 if(link.charAt(0) == '/')
			     p.setPath(link);
			 else
			     p.setPath(path + link);
		     }
		     else {
			 //log.debug("***Parsing link as file:"+link);
			 if(link.charAt(0) == '/')
			     p.setPath(link);
			 else
			     p.setPath(path + link);
		     }
		 }
		 */
		return p;
	    }
	    catch(NoSuchElementException e) {
		throw new FTPException("Invalid server response : "+e.getMessage());
	    }
	    catch(StringIndexOutOfBoundsException e) {
		throw new FTPException("Invalid server response : "+e.getMessage());
	    }
	}


	private long parseUnixListDate(String month, String day, String year2time) throws NumberFormatException {

	    // Nov  9  1998
     // Nov 12 13:51
	    Calendar c = Calendar.getInstance();
	    month = month.toUpperCase();
	    for(int m=0;m<12;m++) {
		if(month.equals(months[m])) {
		    if(year2time.indexOf(':')!= -1) {
			// current year
			c.setTime(new Date(System.currentTimeMillis()));
			StringTokenizer toker = new StringTokenizer(year2time,":");
			// date and time
			c.set(c.get(Calendar.YEAR), m,
	 Integer.parseInt(day),
	 Integer.parseInt(toker.nextToken()),
	 Integer.parseInt(toker.nextToken()));
		    }
		    else {
			// date
			c.set(Integer.parseInt(year2time), m, Integer.parseInt(day),0,0);
		    }
		    break;
		}
	    }
	    return c.getTime().getTime();
	}

	// UTILITY METHODS

	private boolean isValidLink(String link) {
	    return link.indexOf("->") != -1;
	}

	private boolean isValidLine(String l) {
	    String line = l.trim();
	    if(line.equals("")) {
		return false;
	    }
	    /* When decoding, it is important to note that many implementations include a line at the start like "total <number>". Clients should ignore any lines that don't match the described format.
		*/
	    if( line.indexOf("total") != -1 ) {
		try {
		    Integer.parseInt(line.substring(line.lastIndexOf(' ') + 1));
		    return false;
		}
		catch(NumberFormatException e) {
		    // return true // total must be name of real file
		}
	    }
	    return true;
	}

	private int jumpWhiteSpace(String line, int index) {
	    while(line.substring(index, index + 1).equals(" ")) {
		index++;
	    }
	    return index;
	}
    }    
}
