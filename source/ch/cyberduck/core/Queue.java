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

	private Path root;

	public Path getRoot() {
		return this.root;
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
	private long current = -1;
	private long size = -1;

	private String status = "";
	private String error = "";

	public Queue(Path root, int kind) {
		log.debug("Queue");
		this.root = root;
		this.kind = kind;
		this.init();
	}

	public Queue(NSDictionary dict) {
		Host host = new Host((NSDictionary) dict.objectForKey("Host"));
		NSArray elements = (NSArray) dict.objectForKey("Roots");
		log.debug("-");
		for (int i = 0; i < elements.count(); i++) {
			// only one root for now - can be extended in a later version
			this.root = PathFactory.createPath(SessionFactory.createSession(host), (NSDictionary) elements.objectAtIndex(i));
		}
		this.kind = Integer.parseInt((String) dict.objectForKey("Kind"));
		this.status = (String) dict.objectForKey("Status");
		this.size = Integer.parseInt((String) dict.objectForKey("Size"));
		this.current = Integer.parseInt((String) dict.objectForKey("Current"));
		this.init();
	}

	public NSDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.status, "Status");
		dict.setObjectForKey(this.kind + "", "Kind");
		dict.setObjectForKey(this.getSize() + "", "Size");
		dict.setObjectForKey(this.getCurrent() + "", "Current");
		dict.setObjectForKey(this.root.getHost().getAsDictionary(), "Host");
		NSMutableArray list = new NSMutableArray();
		list.addObject(this.root.getAsDictionary());
		dict.setObjectForKey(list, "Roots");
		return dict;
	}

	public int kind() {
		return this.kind;
	}

	public void callObservers(Object arg) {
		//	log.debug(this.countObservers()+" observers known.");
		this.setChanged();
		this.notifyObservers(arg);
	}

	public void update(Observable o, Object arg) {
		//Forwarding all messages from the current file's status to my observers
		if (arg instanceof Message) {
			Message msg = (Message) arg;
			if (msg.getTitle().equals(Message.PROGRESS))
				this.status = (String) msg.getContent();
			if (msg.getTitle().equals(Message.ERROR))
				this.error = " : "+(String) msg.getContent();
		}
		this.callObservers(arg);
	}

	private void process() {
		log.debug("process");
		if (!this.isCanceled()) {
			Iterator elements = jobs.iterator();
			while (elements.hasNext() && !this.isCanceled()) {
				this.progressTimer.start();
				this.leftTimer.start();

				this.currentJob = (Path) elements.next();
				this.currentJob.status.setResume(root.status.isResume());
				this.currentJob.status.addObserver(this);

				this.processedJobs++;

				switch (kind) {
					case KIND_DOWNLOAD:
						currentJob.download();
						break;
					case KIND_UPLOAD:
						currentJob.upload();
						break;
				}
				if (currentJob.status.isComplete()) {
					this.completedJobs++;
				}
				this.currentJob.status.deleteObserver(this);

				this.progressTimer.stop();
				this.leftTimer.stop();
			}
//			if (this.isEmpty()) {
//				root.getSession().close();
//			}
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
		new Thread() {
			public void run() {
				Queue.this.addObserver(observer);

				root.getSession().addObserver(Queue.this);

				elapsedTimer.start();
				running = true;
				callObservers(new Message(Message.QUEUE_START, Queue.this));

				jobs = new ArrayList();
				if (validator.validate(root, kind)) {
					log.debug("Filling this of root element " + root);
					root.fillQueue(jobs, kind);
					process();
				}
				
				elapsedTimer.stop();
				running = false;
				callObservers(new Message(Message.QUEUE_STOP, Queue.this));

				root.getSession().close();

				root.getSession().deleteObserver(Queue.this);
				
				Queue.this.deleteObserver(observer);
			}
		}.start();
	}

	/**
	 * Stops the currently running thread processing the queue.
	 * @pre The thread must be running
	 */
	public void cancel() {
		this.running = false;
		currentJob.status.setCanceled(true);
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
//		log.debug("remainingJobs:");
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

	/**
	 * @return Number of jobs in the this.
	 */
	public int numberOfJobs() {
		return jobs.size();
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
		//		if(this.isRunning()) {
		long value = 0;
		Iterator elements = jobs.iterator();
		while (elements.hasNext()) {
			value += ((Path) elements.next()).status.getSize();
		}
		//		}
		if (value > 0)
			this.size = value;
		return this.size;
	}

	public String getSizeAsString() {
		if (this.getSize() != -1) //@todo performance
			return Status.getSizeAsString(this.getSize());
		return null;
	}

	/**
	 * @return The number of bytes already processed of all elements in the whole this.
	 */
	public long getCurrent() {
		return this.calculateCurrentSize();
	}

	private long calculateCurrentSize() {
		long value = 0;
		Iterator elements = jobs.iterator();
		while (elements.hasNext()) {
			value += ((Path) elements.next()).status.getCurrent();
		}
		if (value > 0)
			this.current = value;
		return this.current;
	}

	public String getCurrentAsString() {
		if (this.getCurrent() != -1) //@todo performance
			return Status.getSizeAsString(this.getCurrent());
		return null;
	}

	/**
	 * @return double current bytes/second
	 */
	public String getSpeedAsString() {
		if (this.isRunning())
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
		this.callObservers(new Message(Message.DATA));
	}

	private void setTimeLeft(int seconds) {
		this.timeLeft = seconds;
		this.callObservers(new Message(Message.PROGRESS));
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
				    Queue.this.callObservers(new Message(Message.CLOCK));
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

		this.jobs.add(this.root);
		this.setChanged();
	}
}