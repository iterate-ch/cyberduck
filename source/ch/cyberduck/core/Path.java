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
import java.util.List;

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
	private List cache = null;
	public Status status = new Status();
	public Attributes attributes;

	public static final String FILE = "FILE";
	public static final String FOLDER = "FOLDER";
	public static final String LINK = "LINK";

	public static final String HOME = "~";

	public Path(NSDictionary dict) {
		log.debug("Path");
		this.setPath((String) dict.objectForKey("Remote"));
		this.setLocal(new Local((String) dict.objectForKey("Local")));
		this.attributes = new Attributes((NSDictionary)dict.objectForKey("Attributes"));
		//this.setStatus(new Status((NSDictionary)dict.objectForKey("Status")));
	}

	public NSDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.getAbsolute(), "Remote");
		dict.setObjectForKey(this.getLocal().toString(), "Local");
		dict.setObjectForKey(this.attributes.getAsDictionary(), "Attributes");
		//dict.setObjectForKey(this.status.getAsDictionary(), "Status");
		return dict;
	}

	/**
	 * A remote path where nothing is known about a local equivalent.
	 * @param path the absolute directory
	 * @param name the file relative to param path
	 */
	public Path(String parent, String name) {
		this.setPath(parent, name);
		this.attributes = new Attributes();
	}

	/**
	 * A remote path where nothing is known about a local equivalent.
	 * @param path The absolute path of the remote file
	 */
	public Path(String path) {
		log.debug("Path");
		this.setPath(path);
		this.attributes = new Attributes();
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
		this.attributes = new Attributes();
	}

	/**
	 * Copies the current path with its attributes but without the status information
	 * @param session The session this path will use to fullfill its tasks
	 * @return A copy of me with a new session
	 */
	public abstract Path copy(Session session);

	public void setPath(String parent, Local file) {
		this.setPath(parent, file.getName());
		this.setLocal(file);
	}

	/**
	 * @param parent The parent directory
	 * @param name The relative filename
	 */
	public void setPath(String parent, String name) {
//		log.debug("setPath:"+parent+","+name);
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
				dirname = this.getSession().workdir().getAbsolute();
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
		return this.cache;
	}

	public void setCache(List files) {
		this.cache = files;
	}

	/**
	 * Request a file listing from the server. Has to be a directory
	 * @param notifyobservers Notify the observers if true
	 */
	public abstract List list(boolean notifyobservers, boolean showHidden);

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

	//    public abstract int size();

	public abstract void rename(String n);

	public abstract void changeOwner(String owner, boolean recursive);

	public abstract void changeGroup(String group, boolean recursive);

	/**
	 * @param recursive Include subdirectories and files
	 */
	public abstract void changePermissions(Permission perm, boolean recursive);

	public boolean isFile() {
		return this.attributes.permission.getMask().charAt(0) == '-';
	}

	/**
	 * @return true if is directory or a symbolic link that everyone can execute
	 */
	public boolean isDirectory() {
		if (this.isLink())
			return this.attributes.permission.getOtherPermissions()[Permission.EXECUTE];
		return this.attributes.permission.getMask().charAt(0) == 'd';
	}

	public boolean isLink() {
		return this.attributes.permission.getMask().charAt(0) == 'l';
	}

	/**
	 * @return The file type
	 */
	public String getKind() {
		if (this.isFile())
			return "File";
		if (this.isDirectory())
			return "Folder";
		if (this.isLink())
			return "Link";
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
			return name.substring(index, name.length());
		return null;
	}

	public abstract Session getSession();

	public abstract void download();

	public abstract void upload();

	public abstract void fillQueue(List queue, int kind);

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
	}

	/**
	 * @param reader The stream to read from
	 * @param writer The stream to write to
	 */
	private void transfer(java.io.Reader reader, java.io.Writer writer) throws IOException {
		LineNumberReader in = new LineNumberReader(reader);
		BufferedWriter out = new BufferedWriter(writer);

		this.status.reset();

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
		// close streams
		if (in != null) {
			in.close();
		}
		if (out != null) {
			out.flush();
			out.close();
		}
	}

	/**
	 * @param i The stream to read from
	 * @param o The stream to write to
	 */
	private void transfer(java.io.InputStream i, java.io.OutputStream o) throws IOException {
		BufferedInputStream in = new BufferedInputStream(i);
		BufferedOutputStream out = new BufferedOutputStream(o);
//		BufferedInputStream in = new BufferedInputStream(new DataInputStream(i));
//		BufferedOutputStream out = new BufferedOutputStream(new DataOutputStream(o));

		this.status.reset();

		// do the retrieving
		int chunksize = Integer.parseInt(Preferences.instance().getProperty("connection.buffer"));
		byte[] chunk = new byte[chunksize];
		int amount = 0;
		long current = this.status.getCurrent();
		boolean complete = false;
		// read from socket (bytes) & write to file in chunks
		while (!complete && !status.isCanceled()) {
			// Reads up to len bytes of data from the input stream into  an array of bytes.  An attempt is made to read as many as  len bytes, but a smaller number may be read, possibly  zero. The number of bytes actually read is returned as an integer.
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
		// close streams
		if (in != null) {
			in.close();
		}
		if (out != null) {
			out.flush();
			out.close();
		}
	}

	public boolean equals(Object other) {
		if (other instanceof Path) {
			Path path = (Path) other;
			return this.getAbsolute().equals(path.getAbsolute());
		}
		if (other instanceof Local) {
			Local local = (Local) other;
			return this.getName().equals(local.getName()) && this.attributes.getModified().equals(local.getModified());
		}
		return false;
	}

	public String toString() {
		return this.getAbsolute();
	}
}
