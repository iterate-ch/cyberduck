package ch.cyberduck.core;

/*
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

import ch.cyberduck.core.Message;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.Iterator;
import java.util.Calendar;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.apache.log4j.Logger;

/**
* Used to queue multiple connections. <code>queue.start()</code> will
 * start the the connections in the order the have been added to me.
 * Useful for actions where the reihenfolge of the taken actions
 * is important, i.e. deleting directories or uploading directories.
 * @version $Id$
 */
public class Queue extends Observable implements Observer { //Thread {
    private static Logger log = Logger.getLogger(Queue.class);

    public static final int KIND_DOWNLOAD = 0;
    public static final int KIND_UPLOAD = 1;

    /**
	* The elements (jobs to process) of the queue
     */
    private Vector files = new java.util.Vector();
    /**
	* What kind of queue, either upload or download
     */
    private int kind;
    /**
	* Number of completed jobs in the queue
     */
    private int completedJobs = 0;
    /**
	* The file currently beeing processed in the queue
     */
    private Path candidate;

    /**
	* The queue has been stopped from processing for any reason
     */
    private boolean stopped;
    /*
     * 	c speed (bytes/second)
     */
    private transient double speed = 0;
    /*
     * overall speed (bytes/second)
     */
    private transient double overall = 0;
    /**
	* The size of all files accumulated
     */
    private int size = -1;

    Calendar calendar = Calendar.getInstance();

    /**
	* @param file The base file to build a queue for. If this is a not a folder
     * the queue will consist of only this.
     * @param  kind Specifiying a download or upload.
     */
    public Queue(Path file, int kind) {
	this.candidate = file;
	this.kind = kind;
    }

    public void callObservers(Message arg) {
//	log.debug(this.countObservers()+" observers known.");
        this.setChanged();
	this.notifyObservers(arg);
    }
    
    public void update(Observable o, Object arg) {
	//Forwarding all messages from the current file's status to my observers
	this.callObservers((Message)arg);
    }

    /**
	* @param file Add path to the queue for later processement.
     */
    public void add(Path file) {
	log.info("Adding file to queue:"+file);
        files.add(file);
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
	* Process the queue. All files will be downloaded or uploaded rerspectively.
     */
    public void start() {
	this.callObservers(new Message(Message.START));
	this.speed = 0;
	this.overall = 0;
	this.stopped = false;
	calendar.set(Calendar.HOUR, 0);
	calendar.set(Calendar.MINUTE, 0);
	calendar.set(Calendar.SECOND, 0);
//	this.current = this.isResume() ? current : 0;
	new Thread() {
	    public void run() {
		Timer clockTimer = new Timer(1000,
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
					   callObservers(new Message(Message.CLOCK, parseTime(calendar.get(Calendar.MINUTE)) + ":" + parseTime(calendar.get(Calendar.SECOND))));
				       }
				   }
			       }
			       );
		clockTimer.start();

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
		overallSpeedTimer.start();

		Timer currentSpeedTimer = new Timer(500,
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
						  setSpeed((diff/speeds.length));
					      }
					      
				       }
				   }
				      );
		currentSpeedTimer.start();

		//Iterating over all the files in the queue
		Iterator i = files.iterator();
		candidate = null;
		while(i.hasNext() && !isStopped()) {
		    candidate = (Path)i.next();
		    String k = KIND_DOWNLOAD == kind ? "Downloading " : "Uploading ";
		    callObservers(new Message(Message.PROGRESS, k+candidate.getName()+" ("+(completedJobs()+1)+" of "+numberOfJobs()+")"));
		    candidate.status.addObserver(Queue.this);
//		    candidate.getSession().addObserver(Queue.this);
		    switch(kind) {
			case KIND_DOWNLOAD:
			    candidate.download();
			    break;
			case KIND_UPLOAD:
			    candidate.upload();
			    break;
		    }
		    if(candidate.status.isComplete()) {			
			callObservers(new Message(Message.COMPLETE));
		    }
//		    candidate.status.deleteObserver(Queue.this);
//		    candidate.getSession().deleteObserver(Queue.this);
		    completedJobs++;
		}

		clockTimer.stop();
		overallSpeedTimer.stop();
		currentSpeedTimer.stop();

		candidate.getSession().close();
		callObservers(new Message(Message.STOP));
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

//    public boolean done() {
//	return this.numberOfElements() == numberOfCompletedJobs;
  //  }

    /**
	*@ return The number of elements in the queue
     */
    public int numberOfJobs() {
//	log.debug("numberOfJobs:"+files.size());
	return files.size();
    }

    /**
	* The number of remaining items to be processed in the queue.
     */
    public int remainingJobs() {
	return this.numberOfJobs() - this.completedJobs;
    }

    public int completedJobs() {
	return this.completedJobs;
    }

    /**
	* @return The cummulative file sizes
     */
    public int getSize() {
	if(-1 == this.size) {
	    this.size = 0;
	    Iterator i = files.iterator();
	    Path file = null;
	    while(i.hasNext()) {
		file = (Path)i.next();
		this.size = this.size + file.status.getSize();
	    }
	}
//	log.debug("getSize:"+size);
	return this.size;
    }

    /**
	* @return The number of bytes already processed.
     */
    public int getCurrent() {
	//@todo is it worth? ouch, calculating...
	int current = 0;
	Iterator i = files.iterator();
	Path file = null;
	while(i.hasNext()) {
	    file = (Path)i.next();
	    current = current + file.status.getCurrent();
	}
//	log.debug("getCurrent:"+current);
	return current;
    }

    /**
	* @return double current bytes/second
     */
    private double getSpeed() {
	return this.speed;
    }
    
    private void setSpeed(double s) {
	this.speed = s;

	this.callObservers(new Message(Message.SPEED, "Current: "
				+ Status.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
				+ Status.parseDouble(this.getOverall()/1024) + " kB/s."));// \n" + this.getTimeLeftMessage());
	//@todo duplicated code
//	if(this.getSpeed() <= 0 && this.getOverall() <= 0) {
//	    this.callObservers(new Message(Message.DATA, Status.parseDouble(this.getCurrent()/1024) + " of " + Status.parseDouble(this.getSize()/1024) + " kBytes."));
//	}
//	else {
//	    if(this.getOverall() <= 0) {
//		this.callObservers(new Message(Message.DATA, Status.parseDouble(this.getCurrent()/1024) + " of "
//				 + Status.parseDouble(this.getSize()/1024) + " kBytes. Current: " +
//				 + Status.parseDouble(this.getSpeed()/1024) + "kB/s.")); //\n" + this.getTimeLeftMessage());
//	    }
//	    else {
//	this.callObservers(new Message(Message.SPEED, Status.parseDouble(this.getCurrent()/1024) + " of "
//				+ Status.parseDouble(this.getSize()/1024) + " kBytes. Current: "
//				+ Status.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
//				+ Status.parseDouble(this.getOverall()/1024) + " kB/s."));// \n" + this.getTimeLeftMessage());
//	    }
//	}
    }

    /**
	* @return double bytes per seconds transfered since the connection has been opened
     */
    private double getOverall() {
	return this.overall;
    }
    
    private void setOverall(double s) {
	this.overall = s;

	this.callObservers(new Message(Message.SPEED, "Current: "
				+ Status.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
				+ Status.parseDouble(this.getOverall()/1024) + " kB/s."));// \n" + this.getTimeLeftMessage());
	    
//	if(this.getSpeed() <= 0 && this.getOverall() <= 0) {
//	    this.callObservers(new Message(Message.DATA, Status.parseDouble(this.getCurrent()/1024) + " of " + Status.parseDouble(this.getSize()/1024) + " kBytes."));
//	}
//	else {
//	    if(this.getOverall() <= 0) {
//		this.callObservers(new Message(Message.DATA, Status.parseDouble(this.getCurrent()/1024) + " of "
//				 + Status.parseDouble(this.getSize()/1024) + " kBytes. Current: " +
//				 + Status.parseDouble(this.getSpeed()/1024) + "kB/s.")); //\n" + this.getTimeLeftMessage());
//	    }
//	    else {
//		this.callObservers(new Message(Message.DATA, Status.parseDouble(this.getCurrent()/1024) + " of "
//				 + Status.parseDouble(this.getSize()/1024) + " kBytes. Current: "
//				 + Status.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
//				 + Status.parseDouble(this.getOverall()/1024) + " kB/s."));// \n" + this.getTimeLeftMessage());
//	    }
//	}
    }
}