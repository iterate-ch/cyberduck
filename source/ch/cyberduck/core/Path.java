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

import java.text.DateFormat;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import java.util.Observable;
import java.util.Observer;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.ObserverList;
import org.apache.log4j.Logger;

/**
 * A path is a remote directory or file.
 * @version $Id$
 */
public abstract class Path extends Observable {//implements Serializable {//, Transferable {
    private static Logger log = Logger.getLogger(Path.class);

    protected String name = null;
    protected String path = null;
    protected Path parent = null;
    private List cache = null;
    public Status status = new Status();
    public Attributes attributes = new Attributes();

    public static String FILE = "FILE";
    public static String FOLDER = "FOLDER";
    public static String LINK = "LINK";
    
    /**
     * @param path the absolute directory
     * @param name the file relative to param path
     * @return Path new instance
     */
    public Path(String path, String name) {
        if(path.charAt(path.length()-1) == '/')
            this.setPath(path + name);
        else
            this.setPath(path + "/" + name);
	ObserverList.instance().registerObservable(this);//@todo only register if download
    }

    /**
     * @param path The absolute path of the file
     */
    public Path(String path) {
        this.setPath(path);
	ObserverList.instance().registerObservable(this);//@todo only register if download
    }

    /**
	* @param pathname The absolute path of the file
     */
    public void setPath(String pathname) {
	log.debug("setPath:"+pathname);
        this.path = pathname.trim();
    }
 
    public void callObservers(Message arg) {
	log.debug("callObservers:"+arg.toString());
	this.setChanged();
	this.notifyObservers(arg);
    }

    /**
	* @return my parent directory
     */
    public abstract Path getParent();

    public List cache() {
	log.debug("cache");
	return this.cache;
    }

    public void setCache(List files) {
	log.debug("setCache");
	this.cache = files;
    }
    
    /**
    * @param refresh Refetch the list from the server
    */
    public abstract void list(boolean refresh);

    public abstract void list();

    public abstract void delete();

    /**
    *	Create a new directory inside me
    * @param folder The relative name of the new folder
    */
    public abstract void mkdir(String folder);
    
//    public abstract void touch(String file);

    public abstract void rename(String n);

    public abstract void download();

    public abstract void upload();

    public abstract void changePermissions(int p);
    
    /**
	* Overwrite this is in the implementation to determine the file type uppon the
     * server response.
     * @return true even if the file doesn't exist on the local filesystem
     * but seems to be a file because there isn't a '/' at
     * the end of the path.
     */


    public boolean isFile() {
	return this.attributes.getMode().charAt(0) == '-';
    }

    public boolean isDirectory() {
	return this.attributes.getMode().charAt(0) == 'd';
    }

    public boolean isLink() {
	return this.attributes.getMode().charAt(0) == 'l';
    }

    /* bad code
    public boolean isFile() {
        String path = this.toString();
        if(path.lastIndexOf('/') == path.length() - 1) {
            return false;
        }
        return true;
    }
     */

    /**
	* Overwrite this is in the implementation to determine the file type uppon the
     * server response.
     * @return true  even if the directory doesn't exist on the local filesystem
     * but seems to be a directory because it ends with '/'
     */
    /* bad code
    public boolean isDirectory() {
//        log.debug("[Path] isDirectory()");
        String path = this.toString();
        if(path.lastIndexOf('/') == path.length() - 1) {
            return true;
        }
        return false;
    }
     */

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
	    String abs = getAbsolute();
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

    public String getAbsoluteEncoded(String path) {
        return java.net.URLEncoder.encode(this.getAbsolute());//, "utf-8");
    }
    
    /**
        * @return The local alias of this path
     */
    public File getLocal() {
        return new File(Preferences.instance().getProperty("download.path"), this.getName());
    }

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
    /*
     public int getPathDepth() {
	 int depth = 0;
	 int length = 0;
        while((length = this.toString().indexOf('/', length + 1)) != -1) {
            depth++;
        }
        return depth;
    }
     */
    
    /*
    * Returns a path relative the parameter
    * @param relative 
    */

    /*@todo
    public Path getRelativePath(Path relative) {
    	int index = this.getAbsolute().indexOf(relative.getAbsolute());
    	if(index == -1) {
    		throw new IllegalArgumentException("The argument must be part of this path");
    	}
    	else {
    		return new Path(this.getAbsolute().substring(index + relative.getAbsolute().length()));
    	}
    }
*/
    /**
     * @ param depth the '/'
     * @ return a new Path cut to the length of parameter <code>depth</code>
     */

    /*@todo
	
    public Path getPathFragment(int depth) throws IllegalArgumentException {
//        log.debug("[Path] getPathFragment(" + depth + ")");
        if(depth > this.getPathDepth())
            throw new IllegalArgumentException("Path is not that long: " + depth + " > " + this.getPathDepth());
        if(depth > 0) {
            int length = 1; //@modification
            for (int n = 0; n < depth; n++) {
                if((length = this.toString().indexOf('/', length + 1)) < 0) {
                    break;
                }
            }
            if(length > 0)
                return new Path(this.toString().substring(0, length + 1));
            else {
                return new Path(this.toString());
            }
        }
        else {
            return new Path("/");
        }
    }
    */

    

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
	//      this.log("Uploading " + action.getParam() + "... (ASCII)", Message.PROGRESS);
        this.transfer(reader, writer);
	//        this.log("Upload of '" + action.getParam() + "' complete", Message.PROGRESS);
    }

    /**
	* binary upload
     * @param i The stream to read from
     * @param o The stream to write to
     */
    public void upload(java.io.OutputStream o, java.io.InputStream i) throws IOException {
        log.debug("upload(" + o.toString() + ", " + i.toString());
	//        this.log("Uploading " + action.getParam() + "... (BINARY)", Message.PROGRESS);
        this.transfer(i, o);
	//        this.log("Upload of '" + action.getParam() + "' complete", Message.PROGRESS);
    }

    /**
	* ascii download
     * @param reader The stream to read from
     * @param writer The stream to write to
     */
    public void download(java.io.Reader reader, java.io.Writer writer) throws IOException {
        log.debug("transfer(" + reader.toString() + ", " + writer.toString());
	//        this.log("Downloading " + bookmark.getServerFilename() + "... (ASCII)", Message.PROGRESS);
        this.transfer(reader, writer);
    }

    /**
	* binary download
     * @param i The stream to read from
     * @param o The stream to write to
     */
    public void download(java.io.InputStream i, java.io.OutputStream o) throws IOException {
        log.debug("transfer(" + i.toString() + ", " + o.toString());
	//        this.log("Downloading " + bookmark.getServerFilename() + "... (BINARY) ", Message.PROGRESS);
        this.transfer(i, o);
    }

    /**
	* @param reader The stream to read from
     * @param writer The stream to write to
     */
    private void transfer(java.io.Reader reader, java.io.Writer writer) throws IOException {
        LineNumberReader in = new LineNumberReader(reader);
        BufferedWriter out = new BufferedWriter(writer);
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
            this.status.setCurrent(this.attributes.getSize());

            //if(action.toString().equals(TransferAction.GET)) {
            //    bookmark.getLocalTempPath().renameTo(bookmark.getLocalPath());
            //    if(Preferences.instance().getProperty("files.postprocess").equals("true")) {
            //        bookmark.open();
            //    }
	    // }
           // this.log("Complete" , Message.PROGRESS);
            this.status.fireCompleteEvent();
        }
        else {
//            this.log("Incomplete", Message.PROGRESS);
            this.status.fireStopEvent();
        }
    }
    
    
    public String toString() {
        return this.getAbsolute();
    }
}
