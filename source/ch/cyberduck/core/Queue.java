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

import com.apple.cocoa.foundation.NSArray;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableArray;
import com.apple.cocoa.foundation.NSMutableDictionary;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * Used to this multiple connections. <code>this.start()</code> will
 * start the the connections in the order the have been added to me.
 * Useful for actions where the order of the taken actions
 * is important, i.e. deleting directories or uploading directories.
 * @version $Id$
 */
public class Queue extends Observable implements Observer { //Thread {
	private static Logger log = Logger.getLogger(Queue.class);

	/**
	 * Estimation time till end of processing
	 */
	private Timer leftTimer;
	/**
	 * File transfer pogress
	 */
	private Timer progressTimer;
	/**
	 * Time left since start of processing
	 */
	private Timer elapsedTimer;

	private Calendar calendar = Calendar.getInstance();

	public static final int KIND_DOWNLOAD = 0;
	public static final int KIND_UPLOAD = 1;
	/**
	 * What kind of this, either KIND_DOWNLOAD or KIND_UPLOAD
	 */
	private int kind;
	/**
	 * Number of jobs handled in the queue. Completed and currently being processed
	 */
	private int processedJobs = 0;
	/**
	 * Number of completed jobs in the queue
	 */
	private int completedJobs = 0;
	/**
	 * The file currently beeing processed in the queue
	 */
	private Path currentJob;

//	public Path getCurrentJob() {
//		return this.currentJob;
//	}
	
	/**
		* The root of the queue; either the file itself or the parent directory of all files
	 */
	private List roots = new ArrayList();
	
	public Path getRoot() {
		return (Path)roots.get(0);
	}

	/**
	 * This has the same size as the roots and contains the root
	 * path itself and all subelements (in case of a directory)
	 */
	private List jobs = new ArrayList();

	/**
	 * The this has been canceled from processing for any reason
	 */
	private boolean running;

	/*
	 * 	current speed (bytes/second)
	 */
	private long speed;

	private long timeLeft = -1;

	private String status = "";
	private String error = "";

	/**
		* @param root Usually the parent directory of serveral files
	 * @param kind Either <code>KIND_DOWNLOAD</code> or <code>KIND_UPLOAD</code>
	 */
//	public Queue(Path root, int kind) {
//		this.root = root;
//		this.kind = kind;
//		this.add(root);
//		this.init();
//	}
	
	/**
		* Creating an empty queue containing no items. Items have to be added later
	 * using the <code>add</code> method.
		* The root will be determined by runtime as the currently processed job
	 * @param kind Either <code>KIND_DOWNLOAD</code> or <code>KIND_UPLOAD</code>
	 */
	public Queue(int kind) {
		this.kind = kind;
		this.init();
	}
	
	public Queue(NSDictionary dict) {
		this(Integer.parseInt((String)dict.objectForKey("Kind")));
		Host host = new Host((NSDictionary) dict.objectForKey("Host"));
		Session s = SessionFactory.createSession(host);
		NSArray r = (NSArray) dict.objectForKey("Roots");
		for (int i = 0; i < r.count(); i++) {
			this.addRoot(PathFactory.createPath(s, (NSDictionary) r.objectAtIndex(i)));
		}
		NSArray items = (NSArray) dict.objectForKey("Items");
		if(null != items) {
			for (int i = 0; i < items.count(); i++) {
				this.jobs.add(PathFactory.createPath(s, (NSDictionary) items.objectAtIndex(i)));
			}
		}
		this.status = (String) dict.objectForKey("Status");
	}

	public NSDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.status, "Status");
		dict.setObjectForKey(this.kind+"", "Kind");
		dict.setObjectForKey(this.getRoot().getHost().getAsDictionary(), "Host");
		NSMutableArray r = new NSMutableArray();
		for (Iterator iter = this.roots.iterator() ; iter.hasNext() ;) {
			r.addObject(((Path)iter.next()).getAsDictionary());
		}
		dict.setObjectForKey(r, "Roots");
		NSMutableArray items = new NSMutableArray();
		for (Iterator iter = jobs.iterator() ; iter.hasNext() ;) {
			items.addObject(((Path)iter.next()).getAsDictionary());
		}
		dict.setObjectForKey(items, "Items");
		return dict;
	}
	
	/**
		* Add an item to the queue
	 * @param item The path to be added in the queue
	 */
	public void addRoot(Path item) {
		log.debug("add:"+item);
		this.roots.add(item);
//		this.currentJob = item;
	}
	
	/**
		* @return Either <code>KIND_DOWNLOAD</code> or <code>KIND_UPLOAD</code>
	 */
	public int kind() {
		return this.kind;
	}

	public void callObservers(Object arg) {
		this.setChanged();
		this.notifyObservers(arg);
	}

	public void update(Observable o, Object arg) {
		if (arg instanceof Message) {
			Message msg = (Message) arg;
			if (msg.getTitle().equals(Message.DATA)) {
				this.callObservers(arg);
			}
			else if (msg.getTitle().equals(Message.PROGRESS)) {
				this.status = (String) msg.getContent();
				this.callObservers(arg);
			}
			else if (msg.getTitle().equals(Message.ERROR)) {
				this.error = " : "+(String) msg.getContent();
				this.callObservers(arg);
			}
		}
	}

	/**
	 * Process the queue. All files will be downloaded or uploaded rerspectively.
	 * @param validator A callback class where the user can decide what to do if
	 * the file already exists at the download location
	 */
	public void start(final Validator validator, final Observer observer) {
		log.debug("start");
		this.completedJobs = 0;
		this.processedJobs = 0;
		this.error = "";
		this.jobs.clear();
		new Thread() {
			public void run() {
				Queue.this.addObserver(observer);
				
				Queue.this.elapsedTimer.start();
				Queue.this.running = true;
				Queue.this.callObservers(new Message(Message.QUEUE_START, Queue.this));
				
				Queue.this.getRoot().getSession().addObserver(Queue.this);
				for (Iterator i = roots.iterator() ; i.hasNext() && !Queue.this.isCanceled(); ) {
					Path r = (Path)i.next();
					log.debug("Iterating over childs of "+r);
					//					for (Iterator k = ((Path)i.next()).getChilds(Queue.this.kind).iterator() ; k.hasNext() && !Queue.this.isCanceled();) {
					Iterator childs = r.getChilds(Queue.this.kind).iterator();
					while(childs.hasNext() && !Queue.this.isCanceled()) {
						log.debug("Adding child to queue...");
						Queue.this.jobs.add((Path)childs.next());
					}
				}
				
				for (Iterator iter = jobs.iterator() ; iter.hasNext() && !Queue.this.isCanceled(); ) {
					Path item = (Path)iter.next();
					log.debug("Validating "+item.toString());
					if (!validator.validate(item, Queue.this.kind)) {
						iter.remove();
					}
					item.status.reset();
				}

				for (Iterator iter = jobs.iterator() ; iter.hasNext() && !Queue.this.isCanceled(); ) {
					Queue.this.run((Path)iter.next());
				}
				
				Queue.this.running = false;
				Queue.this.elapsedTimer.stop();
				Queue.this.callObservers(new Message(Message.QUEUE_STOP, Queue.this));

				Queue.this.getRoot().getSession().close();
				Queue.this.getRoot().getSession().deleteObserver(Queue.this);

				Queue.this.deleteObserver(observer);
			}
		}.start();
	}
	
	private void run(Path job) {
		this.currentJob = job;

		this.progressTimer.start();
		this.leftTimer.start();

		job.status.addObserver(this);
		
		this.processedJobs++;
		switch (kind) {
			case KIND_DOWNLOAD:
				job.download();
				break;
			case KIND_UPLOAD:
				job.upload();
				break;
		}
		if (job.status.isComplete()) {
			this.completedJobs++;
		}
		
		job.status.deleteObserver(this);
		
		this.progressTimer.stop();
		this.leftTimer.stop();
	}
	
	/**
	 * Stops the currently running thread processing the queue.
	 * @pre The thread must be running
	 */
	public void cancel() {
		if(this.isRunning()) {
			this.running = false;
			for (Iterator iter = jobs.iterator() ; iter.hasNext(); ) {
				((Path)iter.next()).status.setCanceled(true);
			}
		}
	}

	/**
	 * @return True if this queue's thread is running
	 */
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * @return True if the processing of the queue has been stopped,
	 * either becasuse the transfers have all been completed or
	 * been cancled by the user.
	 */
	public boolean isCanceled() {
		return !this.isRunning();
	}

	/**
	 * @return The number of remaining items to be processed in the this queue.
	 */
	public int remainingJobs() {
		return this.numberOfJobs() - this.completedJobs();
	}

	/**
	 * @return Number of completed (totally transferred) items in the this queue.
	 */
	public int completedJobs() {
//		log.debug("completedJobs:"+completedJobs);
		return this.completedJobs;
	}

	/**
	 * @return The number of jobs completed or currently being transferred
	 */
	public int processedJobs() {
		return this.processedJobs;
	}
	
	public int numberOfRoots() {
		return this.roots.size();
	}

	/**
	 * @return Number of jobs in the this.
	 */
	public int numberOfJobs() {
//		log.debug("numberOfJobs:"+jobs.size());
		return this.jobs.size();
	}
	
	/**
	 * @return rue if all items in the this have been processed sucessfully.
	 */
	public boolean isEmpty() {
		return this.remainingJobs() == 0;
	}

	public String getStatus() {
		return this.getElapsedTime()+" "+this.status+" "+error;
	}

	public String getProgress() {
		if (this.getCurrentAsString() != null && this.getSizeAsString() != null)
			return this.getCurrentAsString()
			    + " of " +
			    this.getSizeAsString();
		return "";
	}

	/**
	 * @return The cummulative file size of all files remaining in the this
	 */
	public long getSize() {
		return this.calculateTotalSize();
	}


	private long calculateTotalSize() {
		long value = 0;
		for (Iterator iter = jobs.iterator() ; iter.hasNext() ;) {
			value += ((Path) iter.next()).status.getSize();
		}
//		if (value > 0)
//			this.size = value;
//		log.debug(this.toString()+">calculateTotalSize:"+this.size);
		return value;
	}

	public String getSizeAsString() {
		return Status.getSizeAsString(this.getSize());
	}

	/**
	 * @return The number of bytes already processed of all elements in the whole this.
	 */
	public long getCurrent() {
		return this.calculateCurrentSize();
	}

	private long calculateCurrentSize() {
		long value = 0;
		for (Iterator iter = jobs.iterator() ; iter.hasNext() ;) {
			value += ((Path)iter.next()).status.getCurrent();
		}
//		if (value > 0)
//			this.current = value;
//		log.debug(this.toString()+">calculateCurrentSize:"+this.size);
		return value;
	}

	public String getCurrentAsString() {
		return Status.getSizeAsString(this.getCurrent());
	}

	/**
	 * @return double current bytes/second
	 */
	public String getSpeedAsString() {
		if (this.isRunning())
			if (this.getSpeed() > -1)
				return Status.getSizeAsString(this.getSpeed()) + "/sec";
		return "";
	}
	
	/**
	 @return The bytes being processed per second
	 */
	public long getSpeed() {
		return this.speed;
	}

	private void setSpeed(long s) {
		this.speed = s;
//		this.callObservers(new Message(Message.DATA));
	}

	private void setTimeLeft(int seconds) {
		this.timeLeft = seconds;
//		this.callObservers(new Message(Message.PROGRESS));
	}

	public String getTimeLeft() {
		if (this.isRunning()) {
			//@todo: implementation of better 'time left' management.
			if (this.timeLeft != -1) {
				if (this.timeLeft >= 60) {
					return (int) this.timeLeft / 60 + " minutes remaining.";
				}
				else {
					return this.timeLeft + " seconds remaining.";
				}
			}
		}
		return "";
	}

	public String getElapsedTime() {
		if (calendar.get(Calendar.HOUR) > 0) {
			return this.parseTime(calendar.get(Calendar.HOUR))
			    + ":"
			    + parseTime(calendar.get(Calendar.MINUTE))
			    + ":"
			    + parseTime(calendar.get(Calendar.SECOND));
		}
		else {
			return this.parseTime(calendar.get(Calendar.MINUTE))
			    + ":"
			    + parseTime(calendar.get(Calendar.SECOND));
		}
	}

	private String parseTime(int t) {
		if (t > 9) {
			return String.valueOf(t);
		}
		else {
			return "0" + t;
		}
	}

	private void init() {
		log.debug("init");
				
		this.calendar.set(Calendar.HOUR, 0);
		this.calendar.set(Calendar.MINUTE, 0);
		this.calendar.set(Calendar.SECOND, 0);
		this.calendar.set(Calendar.HOUR, 0);
		this.calendar.set(Calendar.MINUTE, 0);
		this.calendar.set(Calendar.SECOND, 0);
		this.elapsedTimer = new Timer(1000,
		    new ActionListener() {
			    int seconds = 0;
			    int minutes = 0;
			    int hours = 0;

			    public void actionPerformed(ActionEvent event) {
				    seconds++;
				    // calendar.set(year, mont, date, hour, minute, second)
				    // >= one hour
				    if (seconds >= 3600) {
					    hours = (int) (seconds / 60 / 60);
					    minutes = (int) ((seconds - hours * 60 * 60) / 60);
					    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), hours, minutes, seconds - minutes * 60);
				    }
				    else {
					    // >= one minute
					    if (seconds >= 60) {
						    minutes = (int) (seconds / 60);
						    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), minutes, seconds - minutes * 60);
					    }
					    // only seconds
					    else {
						    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), seconds);
					    }
				    }
				    Queue.this.callObservers(new Message(Message.PROGRESS));
			    }
		    }
		);

		this.progressTimer = new Timer(500,
		    new ActionListener() {
			    int i = 0;
			    long current;
			    long last;
			    long[] speeds = new long[8];

			    public void actionPerformed(ActionEvent e) {
				    long diff = 0;
				    current = currentJob.status.getCurrent(); // Bytes
				    if (current <= 0) {
					    setSpeed(0);
				    }
				    else {
					    speeds[i] = (current - last) * 2; // Bytes per second
					    i++;
					    last = current;
					    if (i == 8) { // wir wollen immer den schnitt der letzten vier sekunden
						    i = 0;
					    }
					    for (int k = 0; k < speeds.length; k++) {
						    diff = diff + speeds[k]; // summe der differenzen zwischen einer halben sekunde
					    }
					    Queue.this.setSpeed((diff / speeds.length)); //Bytes per second
				    }

			    }
		    }
		);

		this.leftTimer = new Timer(1000,
		    new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				    if (getSpeed() > 0)
					    Queue.this.setTimeLeft((int) ((Queue.this.getSize() - currentJob.status.getCurrent()) / getSpeed()));
				    else
					    Queue.this.setTimeLeft(-1);
			    }
		    }
		);
	}
}