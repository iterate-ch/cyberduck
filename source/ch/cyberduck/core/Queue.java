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

import org.apache.log4j.Logger;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
* Used to queue multiple connections. <code>queue.start()</code> will
 * start the the connections in the order the have been added to me.
 * Useful for actions where the reihenfolge of the taken actions
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

    private Validator validator;

    Calendar calendar = Calendar.getInstance();

    public static final int KIND_DOWNLOAD = 0;
    public static final int KIND_UPLOAD = 1;
    /**
	* What kind of queue, either upload or download
     */
    private int kind;
    /**
	* Number of completed jobs in the queue
     */
    private int completedJobs;
    /**
	* The file currently beeing processed in the queue
     */
    private Path candidate;

    /**
	* This is a list of root path, meaning a directory or file selected to upload.
     */
    private Path[] roots;

    /**
	* This has the same size as the roots and contains the root
     * path itself and all subelements (in case of a directory) 
     */
    private List[] jobs;
    
    /**
	* The queue has been stopped from processing for any reason
     */
    private boolean stopped;
    /*
     * 	current speed (bytes/second)
     */
    private transient double speed;
    /*
     * overall speed (bytes/second)
     */
    private transient double overall;
    /**
	* The size of all files accumulated
     */
    private int size;
    private int current;
    private int timeLeft;
    
    public Queue(Path[] roots, int kind, Validator validator) {
	this.roots = roots;
	this.jobs = new ArrayList[roots.length];
	this.kind = kind;
	this.validator = validator;
	this.init();
    }
    
    /**
	* @param file The base file to build a queue for. If this is a not a folder
     * the queue will consist of only this.
     * @param  kind Specifiying a download or upload.
     */
    public Queue(Path root, int kind, Validator validator) {
	this(new Path[]{root}, kind, validator);
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
	this.callObservers((Message)arg);
    }

//    private void prepare() {
//	for(int i = 0; i < roots.length; i ++) {
//	    roots[i].fillQueue(jobs[i], session, kind);
//	}
//  }

    private void process() {
	log.debug("process");
	Session session = roots[roots.length-1].getSession().copy();
	session.addObserver(Queue.this);

	for(int i = 0; i < roots.length; i ++) {
	    jobs[i] = new ArrayList();
	    log.debug("Filling queue of root element "+roots[i]);
	    roots[i].fillQueue(jobs[i], session, kind);
	}
	
	for(int i = 0; i < roots.length; i ++) {
		//Iterating over all the files in the queue
	    this.callObservers(roots[i]);
	    Iterator elements = jobs[i].iterator();
	    while(elements.hasNext() && !isStopped()) {
		this.progressTimer.start();
		this.leftTimer.start();

		this.candidate = (Path)elements.next();
		this.candidate.status.addObserver(this);

		String content = null;
		if(KIND_DOWNLOAD == kind)
		    content = "Downloading "+candidate.getName()+" ("+(this.completedJobs()+1)+" of "+(this.numberOfJobs())+")";
		else
		    content = "Uploading "+candidate.getName()+" ("+(this.completedJobs()+1)+" of "+(this.numberOfJobs())+")";
		this.callObservers(new Message(Message.PROGRESS, content));
//		this.callObservers(new Message(Message.PROGRESS, KIND_DOWNLOAD == kind ? "Downloading " : "Uploading "+candidate.getName()+" ("+(this.completedJobs()+1)+" of "+this.numberOfJobs())+")");

		if(this.validator.validate(candidate, kind)) {
		    log.debug("Validation sucessfull");
		    switch(kind) {
			case KIND_DOWNLOAD:
			    candidate.download();
			    break;
			case KIND_UPLOAD:
			    candidate.upload();
			    break;
		    }
		    if(candidate.status.isComplete()) {
			current += candidate.status.getCurrent();
			completedJobs++;
		    }
		}
		this.candidate.status.deleteObserver(this);
		
		this.progressTimer.stop();
		this.leftTimer.stop();
	    }
	}

	if(this.numberOfJobs() == this.completedJobs())
	    session.close(); //todo session might be null
	session.deleteObserver(Queue.this);
    }

    /**
	* Process the queue. All files will be downloaded or uploaded rerspectively.
     * @param resume If false finish all non finished items in the queue. If true refill the queue with all the childs from the parent Path and restart
     */
    public void start() {
	log.debug("start");
	this.reset();
	new Thread() {
	    public void run() {
		stopped = false;
		elapsedTimer.start();
//		prepare();
		process();
		elapsedTimer.stop();
		stopped = true;
	    }
	}.start();
    }


    public void cancel() {
	this.stopped = true;
	candidate.status.setCanceled(true);
    }

    public boolean isStopped() {
	return stopped;
    }

    /**
	* @return Number of remaining items to be processed in the queue.
     */
    public int remainingJobs() {
	return this.numberOfJobs() - this.completedJobs();
    }

    /**
	* @return Number of completed (totally transferred) items in the queue.
     */
    public int completedJobs() {
	return this.completedJobs;
    }

    /**
	* @return Size of the queue.
     */
    public int numberOfJobs() {
	int no = 0;
	for(int i = 0; i < roots.length; i ++) {
	    no += this.jobs[i].size();
	}
	return no;
    }

    /**
	* @return rue if all items in the queue have been processed sucessfully.
     */
    public boolean isEmpty() {
	return this.remainingJobs() == 0;
    }

    /**
	* @return The cummulative file size of all files remaining in the queue
     */
    public int getSize() {
//	log.debug("getSize");
	if(-1 ==  this.size)
	    this.calculateSize();
	return this.size;
    }

    private void calculateSize() {
	for(int i = 0; i < roots.length; i ++) {
	    Iterator elements = jobs[i].iterator();
	    while(elements.hasNext()) {
		this.size += ((Path)elements.next()).status.getSize();
	    }
	}
    }
	
    private String parseTime(int t) {
	if(t > 9) {
	    return String.valueOf(t);
        }
        else {
            return "0" + t;
	}
    }

    /**
	* @return The number of bytes already processed.
     */
    public int getCurrent() {
	return this.current + candidate.status.getCurrent();
//	int currentBytes = 0;
//	Iterator i = jobs.iterator();
//	while(i.hasNext()) {
//	    currentBytes += ((Path)i.next()).status.getCurrent();
//	}
//	return currentBytes;
	
//	return candidate.status.getCurrent();
//	int current = 0;
//	Iterator i = files.iterator();
//	Path file = null;
//	while(i.hasNext()) {
//	    file = (Path)i.next();
//	    current = current + file.status.getCurrent();
//	}
//	return current;
    }

    /**
	* @return double current bytes/second
     */
    public double getSpeed() {
	return this.speed;
    }
    
    private void setSpeed(double s) {
	this.speed = s;
	this.callObservers(new Message(Message.DATA, candidate.status));
//	this.callObservers(new Message(Message.SPEED, 
//				Status.parseDouble(this.getSpeed()/1024) + "kB/s, about "
//				+this.getTimeLeft()+" remaining."));
	
//	this.callObservers(new Message(Message.SPEED, "Current: "
//				+ Status.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
//				+ Status.parseDouble(this.getOverall()/1024) + " kB/s. "+this.getTimeLeft()));
    }

    private void setTimeLeft(int seconds) {
        this.timeLeft = seconds;
    }

    public String getTimeLeft() {
        String message = "";
        //@todo: implementation of better 'time left' management.
        if(this.timeLeft != -1) {
            if(this.timeLeft >= 60) {
                message = (int)this.timeLeft/60 + " minutes remaining.";
            }
            else {
                message = this.timeLeft + " seconds remaining.";
            }
        }
        return message;
    }
    
    /**
	* @return double bytes per seconds transfered since the connection has been opened
     */
//    private double getOverall() {
//	return this.overall;
//    }

//private void setOverall(double s) {
//    this.overall = s;
//
//    this.callObservers(new Message(Message.SPEED, "Current: "
//				   + Status.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
//				   + Status.parseDouble(this.getOverall()/1024) + " kB/s. "+this.getTimeLeft()));
//}

private void reset() {
    this.size = -1;
    this.current = -1;
    this.speed = -1;
    this.overall = -1;
    this.timeLeft = -1;
    this.completedJobs = 0;
    this.calendar.set(Calendar.HOUR, 0);
    this.calendar.set(Calendar.MINUTE, 0);
    this.calendar.set(Calendar.SECOND, 0);
}

private void init() {
    this.elapsedTimer = new Timer(1000,
				 new ActionListener() {
				     int seconds = 0;
				     int minutes = 0;
				     int hours = 0;
				     public void actionPerformed(ActionEvent event) {
					 seconds++;
				      // calendar.set(year, mont, date, hour, minute, second)
				// >= one hour
					 if(seconds >= 3600) {
					     hours = (int)(seconds/60/60);
					     minutes = (int)((seconds - hours*60*60)/60);
					     calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), hours, minutes, seconds - minutes*60);
					 }
					 else {
					  // >= one minute
					     if(seconds >= 60) {
						 minutes = (int)(seconds/60);
						 calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), minutes, seconds - minutes*60);
					     }
					  // only seconds
					     else {
						 calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), seconds);
					     }
					 }

					 if(calendar.get(Calendar.HOUR) > 0) {
					     callObservers(new Message(Message.CLOCK, parseTime(calendar.get(Calendar.HOUR)) + ":" + parseTime(calendar.get(Calendar.MINUTE)) + ":" + parseTime(calendar.get(Calendar.SECOND))));
					 }
					 else {
					     Queue.this.callObservers(new Message(Message.CLOCK, parseTime(calendar.get(Calendar.MINUTE)) + ":" + parseTime(calendar.get(Calendar.SECOND))));
					 }
				     }
				 }
				 );

    /*
     Timer overallSpeedTimer = new Timer(4000,
					 new ActionListener() {
					     Vector overall = new Vector();
					     double current;
					     double last;
					     public void actionPerformed(ActionEvent e) {
						 current = candidate.status.getCurrent();
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
     */

    this.progressTimer = new Timer(500,
					new ActionListener() {
					    int i = 0;
					    int current;
					    int last;
					    int[] speeds = new int[8];
					    public void actionPerformed(ActionEvent e) {
						int diff = 0;
						current = candidate.status.getCurrent();
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
						    Queue.this.setSpeed((diff/speeds.length));
						}

					    }
					}
					);

    this.leftTimer = new Timer(1000,
				    new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					    if(getSpeed() > 0)
						Queue.this.setTimeLeft((int)((candidate.status.getSize() - candidate.status.getCurrent())/getSpeed()));
					    else
						Queue.this.setTimeLeft(-1);
					}
				    }
				    );    

}
    }