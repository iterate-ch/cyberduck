package ch.cyberduck.core.ftp;

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
    
    private static final String TRANSFERTYPE = Preferences.instance().getProperty("connection.transfertype.default");

    class FTPFile extends Path {

	public FTPFile(String parent, String name) {
	    super(parent, name);
	}

	public FTPFile(String path) {
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
		parent = new FTPFile(dirname);
	    }
            log.debug("getParent:"+parent);
	    return parent;
	}
	
	public void list() {
	    this.list(this.cache() == null);
	}
	
	/**
	* Request a file listing from the server. Has to be a directory
	* @param
	*/
	public synchronized void list(boolean refresh) {
	    log.debug("list");
	    if(refresh) {
		new Thread() {
		    public void run() {
			try {
			    FTPSession.this.check();
			    FTPSession.this.log("Listing "+FTPFile.this.getName(), Message.PROGRESS);
			    FTP.setType(FTPTransferType.ASCII);
			    FTP.chdir(getAbsolute());
			    FTPFile.this.setCache(new FTPParser().parseList(FTPFile.this.getAbsolute(), FTP.dir()));
			    FTPSession.this.host.callObservers(FTPFile.this);
			    FTPSession.this.log("Listing complete", Message.PROGRESS);
			}
			catch(FTPException e) {
			    FTPSession.this.log("FTP Error: "+e.getMessage(), Message.ERROR);
			}
			catch(IOException e) {
			    FTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
			}
		    }
		}.start();
	    }
	    else {
		FTPSession.this.host.callObservers(FTPFile.this);
	    }
	}	

	public synchronized void delete() {
	    log.debug("delete");
	    try {
		FTPSession.this.check();
		if(this.isDirectory()) {
		    FTP.chdir(this.getAbsolute());
		    List files = new FTPParser().parseList(this.getAbsolute(), FTP.dir());
		    java.util.Iterator iterator = files.iterator();
		    Path file = null;
		    while(iterator.hasNext()) {
			file = (Path)iterator.next();
			if(file.isDirectory()) {
			    file.delete();
			}
			if(file.isFile()) {
			    FTPSession.this.log("Deleting "+this.getName(), Message.PROGRESS);
			    FTP.delete(file.getName());
			}
		    }
		    FTP.cdup();
		    FTPSession.this.log("Deleting "+this.getName(), Message.PROGRESS);
		    FTP.rmdir(this.getName());
		}
		if(this.isFile()) {
		    FTPSession.this.log("Deleting "+this.getName(), Message.PROGRESS);
		    FTP.delete(this.getName());
		}
		this.getParent().list(true);
	    }
	    catch(FTPException e) {
		FTPSession.this.log("FTP Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		FTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	}

        public synchronized void rename(String filename) {
            log.debug("rename");
            try {
		FTPSession.this.check();
                FTP.chdir(this.getParent().getAbsolute());
		FTPSession.this.log("Renaming "+this.getName()+" to "+filename, Message.PROGRESS);
                FTP.rename(this.getName(), filename);
		this.getParent().list(true);
            }
	    catch(FTPException e) {
		FTPSession.this.log("FTP Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		FTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
        }
        
        public synchronized void mkdir(String name) {
            log.debug("mkdir");
            try {
		FTPSession.this.check();
		FTPSession.this.log("Make directory "+name, Message.PROGRESS);
//                FTP.mkdir(this.getName());
                FTP.mkdir(name);
		this.list(true);
            }
	    catch(FTPException e) {
		FTPSession.this.log("FTP Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		FTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	}

	public synchronized void changePermissions(int permissions) {
	    log.debug("changePermissions");
	    try {
		FTPSession.this.check();
		FTP.site("chmod "+permissions+" "+this.getAbsolute());
	    }
	    catch(FTPException e) {
		FTPSession.this.log("FTP Error: "+e.getMessage(), Message.ERROR);
	    }
	    catch(IOException e) {
		FTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	    }
	}

        public synchronized void download() {
            log.debug("download");
	    new Thread() {
		public void run() {
		    try {
			FTPFile.this.status.fireActiveEvent();
			FTPSession.this.check();
			if(FTPFile.this.isDirectory())
			    this.downloadFolder();
			if(FTPFile.this.isFile())
			    this.downloadFile();
		    }
		    catch(FTPException e) {
			FTPSession.this.log("FTP Error: "+e.getMessage(), Message.ERROR);
		    }
		    catch(IOException e) {
			FTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
		    }
		}

		private void downloadFile() throws IOException {
		    if(TRANSFERTYPE.equals("binary")) {
			FTPSession.this.log("Setting transfer mode to BINARY", Message.PROGRESS);
			FTP.setType(FTPTransferType.BINARY);
			FTPFile.this.status.setSize((int)(FTP.size(FTPFile.this.getName())));
			OutputStream out = new FileOutputStream(FTPFile.this.getLocal(), FTPFile.this.status.isResume());
			if(out == null) {
			    throw new IOException("Unable to buffer data");
			}
			FTPSession.this.log("Opening data stream...", Message.PROGRESS);
			java.io.InputStream in = FTP.getBinary(FTPFile.this.getName(), FTPFile.this.status.isResume() ? FTPFile.this.status.getCurrent() : 0);
			if(in == null) {
			    throw new IOException("Unable opening data stream");
			}
			FTPSession.this.log("Downloading "+FTPFile.this.getName()+"...", Message.PROGRESS);
			FTPFile.this.download(in, out);
			FTP.validateTransfer();
		    }
		    else if(TRANSFERTYPE.equals("ascii")) {
			FTPSession.this.log("Setting transfer type to ASCII", Message.PROGRESS);
			FTP.setType(FTPTransferType.ASCII);
			FTPFile.this.status.setSize((int)(FTP.size(FTPFile.this.getName())));
			java.io.Writer out = new FileWriter(FTPFile.this.getLocal(), FTPFile.this.status.isResume());
			if(out == null) {
			    throw new IOException("Unable to buffer data");
			}
			FTPSession.this.log("Opening data stream...", Message.PROGRESS);
			java.io.Reader in = FTP.getASCII(this.getName(), FTPFile.this.status.isResume() ? FTPFile.this.status.getCurrent() : 0);
			if(in == null) {
			    throw new IOException("Unable opening data stream");
			}
			FTPSession.this.log("Downloading "+FTPFile.this.getName()+"...", Message.PROGRESS);
			FTPFile.this.download(in, out);
			FTP.validateTransfer();
		    }
		    else {
			throw new FTPException("Transfer type not set");
		    }
		}

		private void downloadFolder() throws IOException {
		    java.util.List files = new FTPParser().parseList(FTPFile.this.getAbsolute(), FTP.dir());
		    File dir = FTPFile.this.getLocal();
		    dir.mkdir();
		    java.util.Iterator i = files.iterator();
		    while(i.hasNext()) {
			Path r = (Path)i.next();
			if(r.isDirectory()) {
			    log.debug("changing directory: "+r.toString());
			    FTP.chdir(r.getAbsolute());
			    r.download();
			}
			if(r.isFile()) {
			    log.debug("getting file:"+r.toString());
			    r.download();
			}
		    }
		    log.debug("upping directory");
		    FTP.cdup();
		}
	    }.start();
	}
	    
        public synchronized void upload() {
	    new Thread() {
		public void run() {
		    try {
			FTPFile.this.status.fireActiveEvent();
			FTPSession.this.check();
			if(FTPFile.this.getLocal().isDirectory())
			    this.uploadFolder();
			if(FTPFile.this.getLocal().isFile())
			    this.uploadFile();
		    }
		    catch(FTPException e) {
			FTPSession.this.log("FTP Error: "+e.getMessage(), Message.ERROR);
		    }
		    catch(IOException e) {
			FTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
		    }
		}

		private void uploadFile() throws IOException {
		    FTPFile.this.status.setSize((int)FTPFile.this.getLocal().length());
		    if(TRANSFERTYPE.equals(FTPTransferType.BINARY)) {
			FTPSession.this.log("Setting transfer mode to BINARY.", Message.PROGRESS);
			FTP.setType(FTPTransferType.BINARY);
			java.io.InputStream in = new FileInputStream(FTPFile.this.getLocal());
			if(in == null) {
			    throw new IOException("Unable to buffer data");
			}

			FTPSession.this.log("Opening data stream...", Message.PROGRESS);

			java.io.OutputStream out = FTP.putBinary(FTPFile.this.getName(), FTPFile.this.status.isResume());
			if(out == null) {
			    throw new IOException("Unable opening data stream");
			}
			FTPSession.this.log("Uploading "+FTPFile.this.getName()+"...", Message.PROGRESS);
			FTPFile.this.upload(out, in);
			FTP.validateTransfer();
		    }
		    else if(TRANSFERTYPE.equals(FTPTransferType.ASCII)) {
			FTPSession.this.log("Setting transfer type to ASCII.", Message.PROGRESS);
			FTP.setType(FTPTransferType.ASCII);

			java.io.Reader in = new FileReader(FTPFile.this.getLocal());
			if(in == null) {
			    throw new IOException("Unable to buffer data");
			}

			FTPSession.this.log("Opening data stream...", Message.PROGRESS);
			java.io.Writer out = FTP.putASCII(FTPFile.this.getName(), FTPFile.this.status.isResume());
			if(out == null) {
			    throw new IOException("Unable opening data stream");
			}
			FTPSession.this.log("Uploading "+FTPFile.this.getName()+"...", Message.PROGRESS);
			FTPFile.this.upload(out, in);
			FTP.validateTransfer();
		    }
		    else {
			throw new FTPException("Transfer type not set");
		    }
		}

		private void uploadFolder() throws IOException {
		    FTP.mkdir(FTPFile.this.getName());
		    FTP.chdir(FTPFile.this.getName());
		    File[] files = FTPFile.this.getLocal().listFiles();
		    Path p;
		    for(int i = 0; i < files.length; i++) {
			p = new FTPFile(files[i].getAbsolutePath());
			p.upload();
		    }
		    FTP.cdup();		    
		}
	    }.start();
	}
	/*
	public void setTransferType(String t) {
            this.transfertype = t;
        }

        public String getTransferType() {
            return this.transfertype;
        }
	 */
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
//@todo proxy        System.getProperties().put("proxySet", Preferences.instance().getProperty("connection.proxy"));
//@todo proxy        System.getProperties().put("proxyHost", Preferences.instance().getProperty("connection.proxy.host"));
//@todo proxy        System.getProperties().put("proxyPort", Preferences.instance().getProperty("connection.proxy.port"));
    }

    public synchronized void close() {
	try {
	    if(FTP != null) {
		this.log("Disconnecting...", Message.PROGRESS);
		FTP.quit();
	    }
	    this.log("Disconnected", Message.PROGRESS);
	}
	catch(FTPException e) {
	    FTPSession.this.log("FTP Error: "+e.getMessage(), Message.ERROR);
	}
	catch(IOException e) {
	    FTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
	}
//	host.status.fireStopEvent();
    }

    public synchronized void connect() {
	new Thread() {
	    public void run() {
//		host.status.fireActiveEvent();
		FTPSession.this.log("Opening FTP connection to " + host.getIp()+"...", Message.PROGRESS);
		try {
		    if(Preferences.instance().getProperty("ftp.connectmode").equals("active")) {
			FTP.setConnectMode(FTPConnectMode.ACTIVE);
		    }
		    else {
			FTP.setConnectMode(FTPConnectMode.PASV);
		    }
//@todo proxy		    if(Preferences.instance().getProperty("connection.proxy").equals("true")) {
//			FTP.initSOCKS(Preferences.instance().getProperty("connection.proxy.port"), Preferences.instance().getProperty("connection.proxy.host"));
//		    }
//		    if(Preferences.instance().getProperty("connection.proxy.authenticate").equals("true")) {
//			FTP.initSOCKSAuthentication(Preferences.instance().getProperty("connection.proxy.username"), Preferences.instance().getProperty("connection.proxy.password"));
//		    }
		    FTP.connect(host.getName(), host.getPort());
		    FTPSession.this.log("FTP connection opened", Message.PROGRESS);
		    FTPSession.this.login();
		    FTPSession.this.setConnected(true);
		    FTP.system();
		    String path = host.getWorkdir().equals(Preferences.instance().getProperty("connection.path.default")) ? FTP.pwd() : host.getWorkdir();
		    FTPFile home = new FTPFile(path);
		    home.list();
		}
		catch(FTPException e) {
		    FTPSession.this.log("FTP Error: "+e.getMessage(), Message.ERROR);
		}
		catch(IOException e) {
		    FTPSession.this.log("IO Error: "+e.getMessage(), Message.ERROR);
		}
//		finally {
//		    host.status.fireStopEvent();
//		}
	    }
	}.start();
    }

    private synchronized void login() throws IOException {
	log.debug("login");
	try {
	    this.log("Authenticating as " + host.login.getUsername() + "...", Message.PROGRESS);
	    FTP.login(host.login.getUsername(), host.login.getPassword());
	    this.log("Login successfull.", Message.PROGRESS);
	}
	catch(FTPException e) {
	    this.log("Login failed", Message.PROGRESS);
            if(host.getLogin().loginFailure()) {
                // let's try again with the new values
		this.login();
            }
	    else {
		this.log(e.getMessage(), Message.ERROR);
	    }
	}
    }

    public void check() throws IOException {
	log.debug("check");
	if(!FTP.isAlive()) {
	  //  host.recycle();
	    this.setConnected(false);
	    this.connect();
	}
	while(true) {
	    if(this.isConnected())
		return;
	    this.log("Waiting for connection...", Message.PROGRESS);
	    Thread.yield();
	}
    }
    
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
	    boolean showHidden = Preferences.instance().getProperty("listing.showHidden").equals("true");
	    for(int i = 0; i < list.length; i++) {
		int index = 0;
		String line = list[i].trim();
		if(isValidLine(line)) {
		    Path p = parseListLine(parent, line);
		    String filename = p.getName();
		    if(!(filename.equals(".") || filename.equals(".."))) {
			if(filename.charAt(0) == '.' && !showHidden) {
			    //p.attributes.setVisible(false);
			}
			else
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
		    p.attributes.setOwner(owner);
		    p.attributes.setModified(date);
		    p.attributes.setMode(access);
		    p.attributes.setPermission(new Permission(access));
		    p.status.setSize(size);
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
		p.attributes.setOwner(owner);
		p.attributes.setGroup(group);
		p.attributes.setModified(date);
		p.attributes.setMode(access);
		p.attributes.setPermission(new Permission(access));
		p.status.setSize(Integer.parseInt(size));

		if(isLink(line)) {
		    // the following lines are the most ugly. I just don't know how I can be sure
      // a link is a directory or a file. Now I look if there's a '.' and if we have execute rights.

		    //good chance it is a directory if everyone can execute
		    boolean execute = p.attributes.getPermission().getOtherPermissions()[Permission.EXECUTE];
		    boolean point = false;
		    if(link.length() >= 4) {
			if(link.charAt(link.length()-3) == '.' || link.charAt(link.length()-4) == '.')
			    point = true;
		    }
		    boolean directory = false;
		    if(!point && execute)
			directory = true;

		    if(directory) {
			log.debug("Parsing link as directory:"+link);
			//if(!(link.charAt(link.length()-1) == '/'))
			//    link = link+"/";
			if(link.charAt(0) == '/')
			    p.setPath(link);
			else
			    p.setPath(path + link);
		    }
		    else {
			log.debug("Parsing link as file:"+link);
			if(link.charAt(0) == '/')
			    p.setPath(link);
			else
			    p.setPath(path + link);
		    }
		}
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