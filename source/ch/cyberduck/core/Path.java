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

import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;

/**
 * A path is a remote directory or file.
 * @version $Id$
 */
public abstract class Path {// extends Observable {
    private static Logger log = Logger.getLogger(Path.class);

    private String name = null;
    private String path = null;
    private java.io.File local = null;
    protected Path parent = null;

    private List cache = null;

    public Status status = new Status();
    public Attributes attributes = new Attributes();

    public static String FILE = "FILE";
    public static String FOLDER = "FOLDER";
    public static String LINK = "LINK";

    
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
        this.setPath(path);
    }

    /**
	* Create a new path where you know the local file already exists
     * and the remote equivalent might be created later.
     * The remote filename will be extracted from the local file.
	* @parm parent The absolute path to the parent directory on the remote host
     * @param file The associated local file
     */
    public Path(String parent, java.io.File file) {
        this(parent, file.getName());
	this.setLocal(file);
    }

    /**
	* Change this path later for example if the name has changed
     * @param parent The parent directory
     * @param name The relative filename
     */
    public void setPath(String parent, String name) {
        if(parent.charAt(parent.length()-1) == '/')
            this.setPath(parent + name);
        else
            this.setPath(parent + "/" + name);
    }

    /**
	* @param pathname The absolute path of the file
     */
    public void setPath(String pathname) {
//	log.debug("setPath:"+pathname);
//	if(pathname.charAt(pathname.length()-1) == '/')
//	    pathname = pathname.substring(0, pathname.length()-2);
        this.path = pathname.trim();
    }

    /**
	* @return My parent directory
     */
    public abstract Path getParent();


//    public Path getPreviousPath() {
//        Cyberduck.DEBUG("Content of path history:"+pathHistory.toString());
//        int size = pathHistory.size();
//        if((size != -1) && (size > 1)) {
//            Path p = (Path)pathHistory.get(size-2);
//            //delete the fetched path - otherwise we produce a loop
//            pathHistory.remove(size-1);
//            pathHistory.remove(size-2);
//            return p;
//        }
//        return this.getCurrentPath();
//    }
    

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
    public abstract List list(boolean notifyobservers);

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
    
    /**
	* Create a new emtpy file on the remote host
     */
//    public abstract void touch(String file);

    public abstract void rename(String n);

    /**
	* @param p ocal permissions
     */
    public abstract void changePermissions(int p);

    public boolean isFile() {
	return this.attributes.getMode().charAt(0) == '-';
    }

    /**
	* Returns true if is directory or a symbolic link that everyone can execute
     */
    public boolean isDirectory() {
	if(this.isLink())
	    return this.attributes.getPermission().getOtherPermissions()[Permission.EXECUTE];
	return this.attributes.getMode().charAt(0) == 'd';
    }

    public boolean isLink() {
	return this.attributes.getMode().charAt(0) == 'l';
    }

    /**
	* @return The file type
     */
    public String getKind() {
	if(this.isFile())
	    return "File";
	if(this.isDirectory())
	    return "Folder";
	if(this.isLink())
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
	if(name == null) {
	    String abs = this.getAbsolute();
	    int index = abs.lastIndexOf("/");
	    name = (index > 0) ? abs.substring(index + 1) : abs;
	}
	return name;
    }

    /**
     * @return the absolute path name
     */
    public String getAbsolute() {
	//log.debug("getAbsolute:"+this.path);
	return this.path;
    }

    
//    public String getAbsoluteEncoded(String path) {
  //      return java.net.URLEncoder.encode(this.getAbsolute());//, "utf-8");
  //  }

    public void setLocal(java.io.File file) {
	this.local = file;
    }
    
    /**
        * @return The local alias of this path
     */
    public File getLocal() {
	if(null == this.local)
	    return new File(Preferences.instance().getProperty("connection.download.folder"), this.getName());
	return this.local;
    }

    
//    public BoundedRangeModel getProgressModel() {
//	DefaultBoundedRangeModel m = null;
//	try {
//	    if(this.getSize() < 0) {
//		m = new DefaultBoundedRangeModel(0, 0, 0, 100);
//	    }
//	    m = new DefaultBoundedRangeModel(this.status.getCurrent(), 0, 0, this.getSize());
//	}
//	catch(IllegalArgumentException e) {
//	    m = new DefaultBoundedRangeModel(0, 0, 0, 100);
//	}
//	return m;
//  }
    

    /**
	* @returns the extensdion if any
     */
    public String getExtension() {
	String name = this.getName();
	int index = name.lastIndexOf(".");
	if(index != -1)
	    return name.substring(index, name.length());
	return null;
    }

    /**
	* @return Returns the number of '/' characters in a path
     */
//     public int getPathDepth() {
//	 int depth = 0;
//	 int length = 0;
//        while((length = this.toString().indexOf('/', length + 1)) != -1) {
//            depth++;
//        }
//        return depth;
//    }
    
    /*
    * Returns a path relative the parameter
    * @param relative 
    */
//    public Path getRelativePath(Path relative) {
//    	int index = this.getAbsolute().indexOf(relative.getAbsolute());
//    	if(index == -1) {
//    		throw new IllegalArgumentException("The argument must be part of this path");
//    	}
//    	else {
//    		return new Path(this.getAbsolute().substring(index + relative.getAbsolute().length()));
//    	}
//    }


    /**
     * @ param depth the '/'
     * @ return a new Path cut to the length of parameter <code>depth</code>
     */
//    public Path getPathFragment(int depth) throws IllegalArgumentException {
//        log.debug("[Path] getPathFragment(" + depth + ")");
//        if(depth > this.getPathDepth())
//            throw new IllegalArgumentException("Path is not that long: " + depth + " > " + this.getPathDepth());
//        if(depth > 0) {
//            int length = 1; //@modification
//            for (int n = 0; n < depth; n++) {
//                if((length = this.toString().indexOf('/', length + 1)) < 0) {
//                    break;
//                }
//            }
//            if(length > 0)
//                return new Path(this.toString().substring(0, length + 1));
//            else {
//                return new Path(this.toString());
//            }
//        }
//        else {
//            return new Path("/");
//        }
//    }

    public abstract Session getSession();

    public abstract void download();

    public abstract void upload();

    public abstract void fillDownloadQueue(Queue queue, Session session);

    public abstract void fillUploadQueue(Queue queue, Session session);

//    public abstract Session getDownloadSession();

  //  public abstract Session getUploadSession();

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
        this.transfer(reader, writer);
    }

    /**
	* binary upload
     * @param i The stream to read from
     * @param o The stream to write to
     */
    public void upload(java.io.OutputStream o, java.io.InputStream i) throws IOException {
        log.debug("upload(" + o.toString() + ", " + i.toString());
        this.transfer(i, o);
    }

    /**
	* ascii download
     * @param reader The stream to read from
     * @param writer The stream to write to
     */
    public void download(java.io.Reader reader, java.io.Writer writer) throws IOException {
        log.debug("transfer(" + reader.toString() + ", " + writer.toString());
        this.transfer(reader, writer);
    }

    /**
	* binary download
     * @param i The stream to read from
     * @param o The stream to write to
     */
    public void download(java.io.InputStream i, java.io.OutputStream o) throws IOException {
        log.debug("transfer(" + i.toString() + ", " + o.toString());
        this.transfer(i, o);
    }

    /**
	* @param reader The stream to read from
     * @param writer The stream to write to
     */
    private void transfer(java.io.Reader reader, java.io.Writer writer) throws IOException {
        LineNumberReader in = new LineNumberReader(reader);
        BufferedWriter out = new BufferedWriter(writer);

        this.status.setCanceled(false);
        this.status.setComplete(false);

	int current = this.status.getCurrent();
        boolean complete = false;
        // read/write a line at a time
        String line = null;
        while (!complete && !this.status.isCancled()) {
            line = in.readLine();
            if(line == null) {
                complete = true;
            }
            else {
                this.status.setCurrent(current += line.getBytes().length);
                out.write(line, 0, line.length());
                out.newLine();
            }
        }
	this.status.setComplete(complete);
//        this.eof(complete);
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

	this.status.setCanceled(false);
        this.status.setComplete(false);

        // do the retrieving
        int chunksize = Integer.parseInt(Preferences.instance().getProperty("connection.buffer"));
        byte[] chunk = new byte[chunksize];
        int amount = 0;
        int current = this.status.getCurrent();
        boolean complete = false;

        // read from socket (bytes) & write to file in chunks
        while (!complete && !this.status.isCancled()) {
            amount = in.read(chunk, 0, chunksize);
            if(amount == -1) {
                complete = true;
            }
            else {
                this.status.setCurrent(current += amount);
                out.write(chunk, 0, amount);
            }
        }
	this.status.setComplete(complete);
//        this.eof(complete);
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
//    private void eof(boolean complete) {
//	log.debug("eof:"+complete);
  //      if(complete) {
//            this.status.setCurrent(this.status.getSize());
    //        this.status.fireCompleteEvent();
//        }
//        else {
  //          this.status.fireStopEvent();
    //    }
    //}
    
    public String toString() {
	return this.getAbsolute();
//        return "Local:"+this.getLocal().toString()+",Remote:"+this.getAbsolute();
    }
}
