package ch.cyberduck.core;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
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

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * A path is a remote directory or file.
 * @version $Id$
 */
public abstract class Path {
	private static Logger log = Logger.getLogger(Path.class);

	private String path = null;
	private Local local = null;
	protected Path parent = null;
	public Status status = new Status();
	public Attributes attributes = new Attributes();

    public static final int FILE_TYPE = 0;
    public static final int DIRECTORY_TYPE = 1;
    public static final int SYMBOLIC_LINK_TYPE = 2;
//    public static final int UNKNOWN_TYPE = 3;
	
	public static final String HOME = "~";

	/**
		* Deep copies the current path with its attributes but without the status information
	 * @param session The session this path will use to fullfill its tasks
	 * @return A copy of me with a new session
	 */
	public Path copy(Session s) {
		Path copy = PathFactory.createPath(s, this.getAbsolute());
		copy.attributes = this.attributes;
		copy.status.setSize(this.status.getSize());
		return copy;
	}
	
	public Path(NSDictionary dict) {
		log.debug("Path");
		Object pathObj= dict.objectForKey("Remote");
		if(pathObj != null)
			this.setPath((String) pathObj);
		Object localObj= dict.objectForKey("Local");
		if(localObj != null)
			this.setLocal(new Local((String)localObj));
		Object attributesObj = dict.objectForKey("Attributes");
		if(attributesObj != null)
			this.attributes = new Attributes((NSDictionary)attributesObj);
		Object statusObj = dict.objectForKey("Status");
		if(statusObj != null)
			this.status = new Status((NSDictionary)statusObj);
	}

	public NSDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.getAbsolute(), "Remote");
		dict.setObjectForKey(this.getLocal().toString(), "Local");
		dict.setObjectForKey(this.attributes.getAsDictionary(), "Attributes");
		dict.setObjectForKey(this.status.getAsDictionary(), "Status");
		return dict;
	}

	public Path() {
		//
	}

	/**
	 * A remote path where nothing is known about a local equivalent.
	 * @param path the absolute directory
	 * @param name the file relative to param path
	 */
	public Path(String parent, String name) {
		this.setPath(parent, name);
	}

	/**
	 * A remote path where nothing is known about a local equivalent.
	 * @param path The absolute path of the remote file
	 */
	public Path(String path) {
		log.debug("Path");
		this.setPath(path);
	}

	/**
	 * Create a new path where you know the local file already exists
	 * and the remote equivalent might be created later.
	 * The remote filename will be extracted from the local file.
	 * @param parent The absolute path to the parent directory on the remote host
	 * @param file The associated local file
	 */
	public Path(String parent, Local file) {
		this.setPath(parent, file);
	}

	public void setPath(String parent, Local file) {
		this.setPath(parent, file.getName());
		this.setLocal(file);
	}

	/**
	 * @param parent The parent directory
	 * @param name The relative filename
	 */
	public void setPath(String parent, String name) {
		if (parent.charAt(parent.length() - 1) == '/')
			this.setPath(parent + name);
		else
			this.setPath(parent + "/" + name);
	}

	public void setPath(String p) {
		log.debug("setPath:" + p);
		if (p.length() > 1 && p.charAt(p.length() - 1) == '/')
			this.path = p.substring(0, p.length() - 1);
		else
			this.path = p;
	}

	/**
	 * @return My parent directory
	 */
	public Path getParent() {
		String abs = this.getAbsolute();
		if ((null == parent)) {
			int index = abs.lastIndexOf('/');
			String dirname = abs;
			if (index > 0)
				dirname = abs.substring(0, index);
			else if (index == 0) //parent is root
				dirname = "/";
			else if (index < 0)
				dirname = "/";
			parent = PathFactory.createPath(this.getSession(), dirname);
		}
		log.debug("getParent:" + parent);
		return parent;
	}
	
	/**
	 * @throws NullPointerException if session is not initialized
	 */
	public Host getHost() {
		return this.getSession().getHost();
	}

	/**
	 * @return My directory listing
	 */
	public List cache() {
		return this.getSession().cache().get(this.getAbsolute());
	}

	protected void setCache(List files) {
		this.getSession().cache().put(this.getAbsolute(), files);
	}

	/**
	 * Request a file listing from the server. Has to be a directory
	 */
	public abstract List list(boolean refresh, boolean showHidden);

	public abstract List list(boolean refresh);
	
	public abstract List list();

	/**
	 * Remove this file from the remote host. Does not affect
	 * any corresponding local file
	 */
	public abstract void delete();

	/**
	 *	Create a new directory inside me on the remote host
	 * @param folder The relative name of the new folder
	 */
	public abstract Path mkdir(String folder);

	public void rename(String parent, String filename) {
		if (parent.charAt(parent.length() - 1) == '/')
			this.rename(parent + filename);
		else
			this.rename(parent + "/" + filename);
	}
	
	public abstract void rename(String absolute);

	public abstract void changeOwner(String owner, boolean recursive);

	public abstract void changeGroup(String group, boolean recursive);

	/**
	 * @param recursive Include subdirectories and files
	 */
	public abstract void changePermissions(Permission perm, boolean recursive);
	
	public boolean exists() {
		if(this.isRoot())
			return true;
		return this.getParent().exists() && this.getParent().list(false, true).contains(this);
	}

	public boolean isFile() {
		if (this.attributes.isSymbolicLink())
			return this.linksToFile();
		return this.attributes.isFile();
	}
	
//	public abstract void sync(Local local, boolean recursive, boolean commit, int kind);

	/**
	 * @return true if is directory or a symbolic link that everyone can execute
	 */
	public boolean isDirectory() {
		if (this.attributes.isSymbolicLink())
			return this.linksToDirectory();
		return this.attributes.isDirectory();
	}

	private boolean linksToFile() {
		return this.attributes.isSymbolicLink() && this.getName().indexOf(".") != -1;
	}
	
	private boolean linksToDirectory() {
		return !this.linksToFile() && this.attributes.permission.getOwnerPermissions()[Permission.EXECUTE];
	}

	/**
	 * @return The file type
	 */
	public String getKind() {
		if (this.attributes.isSymbolicLink())
			return "Symbolic Link";
		if (this.attributes.isFile())
			return "File";
		if (this.attributes.isDirectory())
			return "Folder";
		return "Unknown";
	}
	
	/**
	 * @return true if this paths points to '/'
	 */
	public boolean isRoot() {
		return this.getAbsolute().equals("/");
	}

	/**
	 * @return The filename if the path is a file
	 * or the full path if it is a directory
	 */
	public String getName() {
		String abs = this.getAbsolute();
		int index = abs.lastIndexOf('/');
		String name = (index > 0) ? abs.substring(index + 1) : abs.substring(1);
		index = name.lastIndexOf('?');
		name = (index > 0) ? name.substring(index + 1) : name;
		return name; 
	}

	/**
	 * @return the absolute path name
	 */
	public String getAbsolute() {
		//log.debug("getAbsolute:"+this.path);
		return this.path;
	}

	public void setLocal(Local file) {
		log.debug("setLocal:" + file);
		this.local = file;
	}

	/**
	 * @return The local alias of this path
	 */
	public Local getLocal() {
		//default value if not set explicitly, i.e. with drag and drop
		if (null == this.local)
			return new Local(Preferences.instance().getProperty("queue.download.folder"), this.getName());
		return this.local;
	}

	/**
	 * @return the extension if any
	 */
	public String getExtension() {
		String name = this.getName();
		int index = name.lastIndexOf(".");
		if (index != -1)
			return name.substring(index+1, name.length());
		return null;
	}

	public abstract Session getSession();

	public abstract void download();

	public abstract void upload();

	/**
		* @return All childs if this file denotes a directory and/or the file itself.
	 */
	public List getChilds(int kind) {
		List childs = new ArrayList();
		switch (kind) {
			case Queue.KIND_DOWNLOAD:
				childs = this.getDownloadQueue(childs);
				break;
			case Queue.KIND_UPLOAD:
				childs = this.getUploadQueue(childs);
				break;
		}
		return childs;
	}
	
	private List getDownloadQueue(List queue) {
		if (this.isDirectory()) {
			for (Iterator i = this.list(false, true).iterator() ; i.hasNext() ;) {
				Path p = (Path) i.next();
				p.setLocal(new Local(this.getLocal(), p.getName()));
				p.getDownloadQueue(queue);
			}
		}
		else if (this.isFile()) {
			queue.add(this);
		}
		return queue;
	}
	
	private List getUploadQueue(List queue) {
		if (this.getLocal().isDirectory()) {
			File[] files = this.getLocal().listFiles();
			for (int i = 0; i < files.length; i++) {
				Path p = PathFactory.createPath(this.getSession(), this.getAbsolute(), new Local(files[i].getAbsolutePath()));
				p.getUploadQueue(queue);
			}
		}
		else if (this.getLocal().isFile()) {
			this.status.setSize(this.getLocal().length()); //setting the file size to the known size of the local file
			queue.add(this);
		}
		return queue;
	}

	// ----------------------------------------------------------
	// Transfer methods
	// ----------------------------------------------------------

	/**
	 * ascii upload
	 * @param reader The stream to read from
	 * @param writer The stream to write to
	 */
	public void upload(java.io.Writer writer, java.io.Reader reader) throws IOException {
		log.debug("upload(" + writer.toString() + ", " + reader.toString());
		this.getSession().log("Uploading " + this.getName() + " (ASCII)", Message.PROGRESS);
		if(this.status.isResume()) {
			long skipped = reader.skip(this.status.getCurrent());
			log.info("Skipping "+skipped+" bytes");
			if(skipped < this.status.getCurrent())
				throw new IOException("Resume failed: Skipped "+skipped+" bytes instead of "+this.status.getCurrent());
		}
		this.transfer(reader, writer);
	}

	/**
	 * binary upload
	 * @param i The stream to read from
	 * @param o The stream to write to
	 */
	public void upload(java.io.OutputStream o, java.io.InputStream i) throws IOException {
		log.debug("upload(" + o.toString() + ", " + i.toString());
		this.getSession().log("Uploading " + this.getName(), Message.PROGRESS);
		if(this.status.isResume()) {
			long skipped = i.skip(this.status.getCurrent());
			log.info("Skipping "+skipped+" bytes");
			if(skipped < this.status.getCurrent())
				throw new IOException("Resume failed: Skipped "+skipped+" bytes instead of "+this.status.getCurrent());
		}
		this.transfer(i, o);
	}

	/**
	 * ascii download
	 * @param reader The stream to read from
	 * @param writer The stream to write to
	 */
	public void download(java.io.Reader reader, java.io.Writer writer) throws IOException {
		log.debug("transfer(" + reader.toString() + ", " + writer.toString());
		this.getSession().log("Downloading " + this.getName() + " (ASCII)", Message.PROGRESS);
		this.transfer(reader, writer);
		if(this.status.isComplete()) {
			this.getLocal().getTemp().renameTo(this.getLocal());
			if (Preferences.instance().getProperty("queue.download.changePermissions").equals("true")) {
				Permission perm = this.attributes.getPermission();
				if(!perm.isUndefined()) {
					this.getLocal().setPermission(perm);
				}
			}
		}
	}

	/**
	 * binary download
	 * @param i The stream to read from
	 * @param o The stream to write to
	 */
	public void download(java.io.InputStream i, java.io.OutputStream o) throws IOException {
		log.debug("transfer(" + i.toString() + ", " + o.toString());
		this.getSession().log("Downloading " + this.getName(), Message.PROGRESS);
		this.transfer(i, o);
		if(this.status.isComplete()) {
			this.getLocal().getTemp().renameTo(this.getLocal());
			if (Preferences.instance().getProperty("queue.download.changePermissions").equals("true")) {
				Permission perm = this.attributes.getPermission();
				if(!perm.isUndefined()) {
					this.getLocal().setPermission(perm);
				}
			}
		}
	}

	/**
	 * @param reader The stream to read from
	 * @param writer The stream to write to
	 */
	private void transfer(java.io.Reader reader, java.io.Writer writer) throws IOException {
		log.debug("transfer(" + reader.toString() + ", " + writer.toString());
		LineNumberReader in = new LineNumberReader(reader);
		BufferedWriter out = new BufferedWriter(writer);

		long current = this.status.getCurrent();
		boolean complete = false;
		// read/write a line at a time
		String line = null;
		while (!complete && !status.isCanceled()) {
			line = in.readLine();
			if (line == null) {
				complete = true;
			}
			else {
				this.status.setCurrent(current += line.getBytes().length);
				out.write(line, 0, line.length());
				out.newLine();
			}
		}
		this.status.setComplete(complete);
	}

	/**
	 * @param i The stream to read from
	 * @param o The stream to write to
	 */
	private void transfer(java.io.InputStream i, java.io.OutputStream o) throws IOException {
		log.debug("transfer(" + i.toString() + ", " + o.toString());
		BufferedInputStream in = new BufferedInputStream(i);
		BufferedOutputStream out = new BufferedOutputStream(o);
		int chunksize = Integer.parseInt(Preferences.instance().getProperty("connection.buffer"));
		byte[] chunk = new byte[chunksize];
		int amount = 0;
		long current = this.status.getCurrent();
		boolean complete = false;
		// read from socket (bytes) & write to file in chunks
		while (!complete && !status.isCanceled()) {
			amount = in.read(chunk, 0, chunksize);
			if (amount == -1) {
				complete = true;
			}
			else {
				this.status.setCurrent(current += amount);
				out.write(chunk, 0, amount);
			}
		}
		this.status.setComplete(complete);
	}

	public boolean equals(Object other) {
		if(other instanceof Path)
			return this.getAbsolute().equals(((Path)other).getAbsolute());
		return false;
	}
	
	public String toString() {
		return this.getAbsolute();
	}
}
