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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Observable;
import java.util.Vector;

/**
  * The Status class is the model of a download's status. The view of this is represented by
  * the <code>StatusPanel</code> notified by the methods available from the <code>Observable</code> class.
  * To get notifed of my status register via <code>registerObserver()</code> of <code>BookmarkPanel</code>.
  * @version $Id$
  */
public class Status extends Observable implements Serializable {

    private static Logger log = Logger.getLogger(Status.class);

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

    private transient Timer currentSpeedTimer, overallSpeedTimer, chronoTimer;//timeLeftTimer;

    private boolean canceled;
    /**
	* Indicates that the last action has been completed.
     */
    private boolean complete = false;
    /**
	* The last action has been stopped, but must not be completed.
     */
    private transient boolean stopped = true;

    private Calendar calendar = Calendar.getInstance();
    
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private int left;
    
    /**
	* The wrapper for any status informations of a transfer like it's length and transferred
     * bytes.
     */
    public Status () {
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
	calendar.set(Calendar.SECOND, 0);
    }

    /**
        * Notify all observers
     * @param arg The message to send to the observers
     * @see ch.cyberduck.core.Message
     */
    public void callObservers(Message arg) {
	log.debug("callObserver:"+arg);
	log.debug(this.countObservers()+" observers known.");
        this.setChanged();
	this.notifyObservers(arg);
    }


    public String parseTime(int t) {
	if(t > 9) {
	    return String.valueOf(t);
        }
        else {
            return "0" + t;
	}
    }

    public double parseDouble(double d) {
        //log.debug("parseDouble(" + d + ")");
        String s = Double.toString(d);
        if(s.indexOf(".") != -1) {
            int l = s.substring(s.indexOf(".")).length();
            if(l > 3) {
                return Double.parseDouble(s.substring(0, s.indexOf('.') + 3));
            }
            else {
                return Double.parseDouble(s.substring(0, s.indexOf('.') + l));
            }
        }
        else {
            return d;
        }
    }

    // ZUSTAENDE
    private void setComplete(boolean b) {
        this.complete = b;
    }
    public boolean isComplete() {
        return this.complete;
    }    
    
    private void setStopped(boolean b) {
        this.stopped = b;
//	this.callObservers(new Message("Idle", Message.PROGRESS));
    }
    public boolean isStopped() {
        return this.stopped;
    } 

    public void setCanceled(boolean b) {
        canceled = b;
	if(this.currentSpeedTimer != null)
	    this.currentSpeedTimer.stop();
	if(this.overallSpeedTimer != null)
	    this.overallSpeedTimer.stop();
	if(this.chronoTimer != null)
	    this.chronoTimer.stop();
	this.fireStopEvent();
    }

    public boolean isCancled() {
        return canceled;
    }

    /**
	* Notify that the connection has been opened.
     */
    public void fireActiveEvent() {
        log.debug("fireActiveEvent()");
        this.reset();
        this.setCanceled(false);
        this.setComplete(false);
        this.setStopped(false);
	this.overallSpeedTimer.start();
	this.currentSpeedTimer.start();
	this.chronoTimer.start();
	this.callObservers(new Message(Message.START));
    }

    /**
	* Notify that the connection has been closed.
     */
    public void fireStopEvent() {
        log.debug("fireStopEvent()");
	
	this.setStopped(true);
	if(this.currentSpeedTimer != null)
	    this.currentSpeedTimer.stop();
	if(this.overallSpeedTimer != null)
	    this.overallSpeedTimer.stop();
	if(this.chronoTimer != null)
	    this.chronoTimer.stop();
	this.setResume(false);
	this.callObservers(new Message(Message.STOP));
    }

/**
* Notify that the transfer has been completed.
     */
    public void fireCompleteEvent() {
        log.debug("fireCompleteEvent()");
	
	this.setStopped(true);
	this.setComplete(true);
	if(this.currentSpeedTimer != null)
	    this.currentSpeedTimer.stop();
	if(this.overallSpeedTimer != null)
	    this.overallSpeedTimer.stop();
	if(this.chronoTimer != null)
	    this.chronoTimer.stop();
	this.setResume(false);
	this.callObservers(new Message(Message.COMPLETE));
    }

    public BoundedRangeModel getProgressModel() {
	DefaultBoundedRangeModel m = null;
	try {
	    if(this.getSize() < 0) {
		m = new DefaultBoundedRangeModel(0, 0, 0, 100);
	    }
	    m = new DefaultBoundedRangeModel(this.getCurrent(), 0, 0, this.getSize());
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

	if(this.getSpeed() <= 0 && this.getOverall() <= 0) {
	    this.callObservers(new Message(Message.DATA, this.parseDouble(this.getCurrent()/1024) + " of " + this.parseDouble(this.getSize()/1024) + " kBytes."));
	}
	else {
	    if(this.getOverall() <= 0) {
		this.callObservers(new Message(Message.DATA, this.parseDouble(this.getCurrent()/1024) + " of "
		    + this.parseDouble(this.getSize()/1024) + " kBytes. Current: " +
		    + this.parseDouble(this.getSpeed()/1024) + "kB/s.")); //\n" + this.getTimeLeftMessage());
	    }
	    else {
		this.callObservers(new Message(Message.DATA, this.parseDouble(this.getCurrent()/1024) + " of "
		    + this.parseDouble(this.getSize()/1024) + " kBytes. Current: "
		    + this.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
		    + this.parseDouble(this.getOverall()/1024) + " kB/s."));// \n" + this.getTimeLeftMessage());
	    }
	}
//	this.callObservers(msg);
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

    public void setResume(boolean resume) {
	this.resume = resume;
    }
    
    public boolean isResume() {
	return this.resume;
    }


    public void reset() {
	this.speed = 0;
	this.overall = 0;
	this.current = this.isResume() ? current : 0;
	if(overallSpeedTimer == null) {
	    overallSpeedTimer = new Timer(4000,
				   new ActionListener() {
				       Vector overall = new Vector();
				       double current;
				       double last;
				       public void actionPerformed(ActionEvent e) {
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

	if(chronoTimer == null) {
	    chronoTimer = new Timer(1000,
			     new ActionListener() {
				 public void actionPerformed(ActionEvent event) {
				      //                    log.debug("chronoTimer:actionPerformed()");
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
	}

	/*
	 if(timeLeftTimer == null) {
	     timeLeftTimer = new Timer(1000,
			    new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				    setTimeLeft((int)((getSize() - getCurrent())/getSpeed()));
				}
			    }
			    );
	 }
	 */
    }

/*
    private void setTimeLeft(int seconds) {
        this.left = seconds;
    }
    private int getTimeLeft() {
        return this.left;
    }

    private String getTimeLeftMessage() {
        int s = this.getTimeLeft();
        String message = "";
        //@todo: implementation of better 'time left' management.
        if(s != -1) {
            if(s >= 60) {
                message = (int)s/60 + " minutes remaining.";
            }
            else {
                message = s + " seconds remaining.";
            }
        }
        return message;
    }
 */
}