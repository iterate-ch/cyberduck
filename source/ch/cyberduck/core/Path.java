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
import ch.cyberduck.core.Preferences;
import org.apache.log4j.Logger;

/**
 * A path is a remote directory or file.
 * @version $Id$
 */
public abstract class Path extends Observable implements Serializable {//, Transferable {

    private static Logger log = Logger.getLogger(Path.class);

    protected String name = null;
    protected String path = null;
    protected Path parent = null;
    private List cache;
    public FileStatus status = new FileStatus();
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
        if(path.charAt(path.length() -1) == '/')
            this.init(path + name);
        else
            this.init(path + "/" + name);
    }

    /**
     * @param path The absolute path of the file
     */
    public Path(String path) {
        this.init(path);
    }

    private void init(String pathname) {
	log.debug("init:"+pathname);
        this.path = pathname.trim();
    }

    public void addObserver(Observer o) {
	this.status.addObserver(o);
	this.attributes.addObserver(o);
	super.addObserver(o);
    }

    public void callObservers(Object arg) {
	//        log.debug("callObservers:"+arg.toString());
	this.setChanged();
	this.notifyObservers(arg);
    }
    

    /**
	* @return my parent directory
     */
    public abstract Path getParent();
    
    /**
     * @return the content of this directory.
     */
    public abstract List list();

    public abstract void delete();

    public abstract void mkdir();

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

    public String getExtension() {
	if(this.isDirectory()) {
	    return "/";
	}
	else {
	    String name = this.getName();
	    int index = name.lastIndexOf(".");
	    if(index != -1)
		return name.substring(index, name.length());
	    return "txt";
	}
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
    // File attributes
    // ----------------------------------------------------------    
    
    public class Attributes extends Observable {
	private int size;
	private long modified;

	private String owner;
	private String group;
	private String access;
	private Permission permission;
	private boolean visible = true;

	/**
	    * @param visible If this path should be shown in the directory listing
	 */
	public void setVisible(boolean visible) {
	    this.visible = visible;
	}
	/**
	    * @return If this path is shown in the directory listing
	 */
	public boolean isVisible() {
	    return this.visible;
	}
	
	/**
	* @ param size the size of file in bytes.
	 */
	public void setSize(int size) {
	    //	log.debug("setSize:"+size);
	    this.size = size;
	}

	/**
	* @ return length the size of file in bytes.
	 */
	public int getSize() {
	    return size;
	}	

	private static final int KILO = 1024; //2^10
	private static final int MEGA = 1048576; // 2^20
	private static final int GIGA = 1073741824; // 2^30
	
	/**
	    * @return The size of the file
	 */
	public String getSizeAsString() {
	    if(size < KILO) {
		return size + " B";
	    }
	    else if(size < MEGA) {
		return new Double(size/KILO).intValue() + " KB";
	    }
	    else if(size < GIGA) {
		return new Double(size/MEGA).intValue() + " MB";
	    }
	    else {
		return new Double(size/GIGA).intValue() + " GB";
	    }
	}

	/**
	    * Set the modfication returned by ftp directory listings
	 */
	public void setModified(long m) {
	    this.modified = m;
	}

	/**
	    * @return the modification date of this file
	 */
	public String getModified() {
	    return (DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)).format(new Date(this.modified));
	}

	/**
	    * @param access unix access permitions, i.e. -rwxrwxrwx
	 */

	public void setMode(String access) {
	    this.access = access;
	}

	/**
	    * @return The unix access permissions including the the first bit
	 */
	protected String getMode() {
	    return this.access;
	}

	public void setPermission(Permission p) {
	    this.permission = p;
	}

	public Permission getPermission() {
	    return this.permission;
	}

	public void setOwner(String o) {
	    this.owner = o;
	}

	public String getOwner() {
	    return this.owner;
	}

	public void setGroup(String g) {
	    this.group = g;
	}

	public String getGroup() {
	    return this.group;
	}

	public String getKind() {
	    if(Path.this.isFile())
		return "File";
	    if(Path.this.isDirectory())
		return "Folder";
	    if(Path.this.isLink())
		return "Link";
	    return "Unknown";
	}
    }
    
    public class FileStatus extends Status {

	/**
	* Download is resumable
	 */
	private transient boolean resume = false;

	private int current = 0;
	/*
	 * current speed (bytes/second)
	 */
	private transient double speed = 0;
	/*
	 * overall speed (bytes/second)
	 */
	private transient double overall = 0;
	/*
	 * the size of the file
	 */
	private int size = -1;

	private transient Timer currentSpeedTimer, overallSpeedTimer;//, timeLeftTimer;
	
	public BoundedRangeModel getProgressModel() {
	    DefaultBoundedRangeModel m = null;
	    try {
		if(attributes.getSize() < 0) {
		    m = new DefaultBoundedRangeModel(0, 0, 0, 100);
		}
		m = new DefaultBoundedRangeModel(this.getCurrent(), 0, 0, attributes.getSize());
	    }
	    catch(IllegalArgumentException e) {
		m = new DefaultBoundedRangeModel(0, 0, 0, 100);
	    }
	    return m;
	}

	public int getCurrent() {
	    return current;
	}

	/**
	* @param c The currently transfered bytes
	 */
	public void setCurrent(int c) {
	    //        log.debug("setCurrent(" + c + ")");
	    this.current = c;

	    Message msg = null;
	    if(this.getSpeed() <= 0 && this.getOverall() <= 0) {
		msg = new Message(Message.DATA, this.parseDouble(this.getCurrent()/1024) + " of " + this.parseDouble(attributes.getSize()/1024) + " kBytes.");
	    }
	    else {
		if(this.getOverall() <= 0) {
		    msg = new Message(Message.DATA, this.parseDouble(this.getCurrent()/1024) + " of "
			+ this.parseDouble(attributes.getSize()/1024) + " kBytes. Current: " +
			+ this.parseDouble(this.getSpeed()/1024) + "kB/s. ");// + this.getTimeLeftMessage();
		}
		else {
		    msg = new Message(Message.DATA, this.parseDouble(this.getCurrent()/1024) + " of "
			+ this.parseDouble(attributes.getSize()/1024) + " kBytes. Current: "
			+ this.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
			+ this.parseDouble(this.getOverall()/1024) + " kB/s. ");// + this.getTimeLeftMessage();
		}
	    }

	    this.callObservers(msg);
	}

	/**
        * @return double current bytes/second
	 */
	private double getSpeed() {
	    return this.speed;
	}
	private void setSpeed(double s) {
	    this.speed = s;
	}

	/**
	* @return double bytes per seconds transfered since the connection has been opened
	 */
	private double getOverall() {
	    return this.overall;
	}
	private void setOverall(double s) {
	    this.overall = s;
	}

	public void setResume(boolean value) {
	    this.resume = value;
	}
	public boolean isResume() {
	    return this.resume;
	}

	public void fireActiveEvent() {
	    super.fireActiveEvent();
	    this.overallSpeedTimer.start();
	    this.currentSpeedTimer.start();
	}

	public void fireStopEvent() {
	    super.fireStopEvent();
	    if(this.currentSpeedTimer != null)
		this.currentSpeedTimer.stop();
	    if(this.overallSpeedTimer != null)
		this.overallSpeedTimer.stop();
	    this.setResume(false);

	}

	public void fireCompleteEvent() {
	    super.fireCompleteEvent();
	    this.currentSpeedTimer.stop();
	    this.overallSpeedTimer.stop();
            this.setResume(false);
	}

	public void reset() {
	    super.reset();
	    this.speed = 0;
	    this.overall = 0;
	    if(overallSpeedTimer == null) {
		overallSpeedTimer = new Timer(4000,
				new ActionListener() {
				    Vector overall = new Vector();
				    double current;
				    double last;
				    public void actionPerformed(ActionEvent e) {
					//                    log.debug("overallSpeedTimer:actionPerformed()");
					current = getCurrent();
					if(current <= 0) {
					    setOverall(0);
					}
					else {
					    overall.add(new Double((current - last)/4)); // bytes transferred for the last 4 seconds
					    Iterator iterator = overall.iterator();
					    double sum = 0;
					    while(iterator.hasNext()) {
						Double s = (Double)iterator.next();
						sum = sum + s.doubleValue();
					    }
					    setOverall((sum/overall.size()));
					    last = current;
					    //                        log.debug("overallSpeed " + sum/overall.size()/1024 + " KBytes/sec");
					}
				    }
				}
				);
	    }

	    if(currentSpeedTimer == null) {
		currentSpeedTimer = new Timer(500,
				new ActionListener() {
				    int i = 0;
				    int current;
				    int last;
				    int[] speeds = new int[8];
				    public void actionPerformed(ActionEvent e) {
					//                    log.debug("currentSpeedTimer:actionPerformed()");
					int diff = 0;
					current = getCurrent();
					if(current <= 0) {
					    setSpeed(0);
					}
					else {
					    speeds[i] = (current - last)*(2); i++; last = current;
					    if(i == 8) { // wir wollen immer den schnitt der letzten vier sekunden
						i = 0;
					    }

					    for (int k = 0; k < speeds.length; k++) {
						diff = diff + speeds[k]; // summe der differenzen zwischen einer halben sekunde
					    }

					    //                        log.debug("currentSpeed " + diff/speeds.length/1024 + " KBytes/sec");
					    setSpeed((diff/speeds.length));
					}
				    }
				}
				);
	    }
	}
	
	public String toString() {
	    return "Status:" + "Stopped=" + isStopped() + ", Complete=" + isComplete() + ", Resume=" + isResume() + ", Current=" + getCurrent() + ", Speed=" + getSpeed() + ", Overall=" + getOverall();
	}
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
