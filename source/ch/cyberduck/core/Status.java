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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Vector;

/**
  * The Status class is the model of a download's status.
 * @version $Id$
  */
public class Status extends Observable implements Serializable {
    private static Logger log = Logger.getLogger(Status.class);

    /**
    * Download is resumable
     */
    private transient boolean resume = false;
    /**
	* The file length
     */
    private int size = -1;
    /**
	* The number of transfered bytes. Must be less or equals size.
     */
    private int current = 0;
    /**
	* Indiciating wheter the transfer has been cancled by the user.
     */
    private boolean canceled;
    /**
	* Indicates that the last action has been completed.
     */
    private boolean complete = false;
    /**
	* The last action has been stopped, but must not be completed.
     */
    
    /**
	* The wrapper for any status informations of a transfer like it's length and transferred
     * bytes.
     */
    public Status () {
//        calendar.set(Calendar.HOUR, 0);
  //      calendar.set(Calendar.MINUTE, 0);
//	calendar.set(Calendar.SECOND, 0);
    }

    /**
        * Notify all observers
     * @param arg The message to send to the observers
     * @see ch.cyberduck.core.Message
     */
    public void callObservers(Message arg) {
//	log.debug("callObserver:"+arg);
//	log.debug(this.countObservers()+" observers known.");
	this.setChanged();
	this.notifyObservers(arg);
    }

    /**
	* @param size the size of file in bytes.
     */
    public void setSize(int size) {
	//	log.debug("setSize:"+size);
	this.size = size;
    }

    /**
	* @return length the size of file in bytes.
     */
    public int getSize() {
//	log.debug("getSize:"+size);
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
    

    // ZUSTAENDE
    public void setComplete(boolean b) {
        this.complete = b;
    }
    public boolean isComplete() {
        return this.complete;
    }    
    
//    private void setStopped(boolean b) {
  //      this.stopped = b;
  //  }
    //public boolean isStopped() {
      //  return this.stopped;
   // } 

    public void setCanceled(boolean b) {
	canceled = b;
//	if(this.currentSpeedTimer != null)
//	    this.currentSpeedTimer.stop();
//	if(this.overallSpeedTimer != null)
//	    this.overallSpeedTimer.stop();
//	if(this.chronoTimer != null)
//	    this.chronoTimer.stop();
//	this.fireStopEvent();
    }

    public boolean isCancled() {
	return canceled;
    }

    /**
	* Notify that the connection has been opened.
     */
//    public void fireActiveEvent() {
  //      log.debug("fireActiveEvent()");
    //    this.reset();
      //  this.setCanceled(false);
       // this.setComplete(false);
//        this.setStopped(false);
//	this.overallSpeedTimer.start();
//	this.currentSpeedTimer.start();
//	this.chronoTimer.start();
//	this.callObservers(new Message(Message.START));
//    }

    /**
	* Notify that the connection has been closed.
     */
//    public void fireStopEvent() {
  //      log.debug("fireStopEvent()");
//	
//	this.setStopped(true);
//	if(this.currentSpeedTimer != null)
//	    this.currentSpeedTimer.stop();
//	if(this.overallSpeedTimer != null)
//	    this.overallSpeedTimer.stop();
//	if(this.chronoTimer != null)
//	    this.chronoTimer.stop();
//	this.setResume(false);
//	this.callObservers(new Message(Message.STOP));
//    }

/**
* Notify that the transfer has been completed.
     */
  //  public void fireCompleteEvent() {
//        log.debug("fireCompleteEvent()");
	
//	this.setStopped(true);
//	this.setComplete(true);
//	if(this.currentSpeedTimer != null)
//	    this.currentSpeedTimer.stop();
//	if(this.overallSpeedTimer != null)
//	    this.overallSpeedTimer.stop();
//	if(this.chronoTimer != null)
//	    this.chronoTimer.stop();
//	this.setResume(false);
//	this.callObservers(new Message(Message.COMPLETE));
//    }

//    public BoundedRangeModel getProgressModel() {
//	DefaultBoundedRangeModel m = null;
//	try {
//	    if(this.getSize() < 0) {
//		m = new DefaultBoundedRangeModel(0, 0, 0, 100);
//	    }
//	    m = new DefaultBoundedRangeModel(this.getCurrent(), 0, 0, this.getSize());
//	}
//	catch(IllegalArgumentException e) {
//	    m = new DefaultBoundedRangeModel(0, 0, 0, 100);
//	}
//	return m;
  //  }

    public int getCurrent() {
	return current;
    }

    /**
	* @param c The currently transfered bytes
     */
    public void setCurrent(int c) {
	//        log.debug("setCurrent(" + c + ")");
	this.current = c;
	this.callObservers(new Message(Message.DATA, Status.parseDouble(this.getCurrent()/1024) + " of " + Status.parseDouble(this.getSize()/1024) + " kBytes."));
/*
	if(this.getSpeed() <= 0 && this.getOverall() <= 0) {
	    this.callObservers(new Message(Message.DATA, Status.parseDouble(this.getCurrent()/1024) + " of " + Status.parseDouble(this.getSize()/1024) + " kBytes."));
	}
	else {
	    if(this.getOverall() <= 0) {
		this.callObservers(new Message(Message.DATA, Status.parseDouble(this.getCurrent()/1024) + " of "
		    + Status.parseDouble(this.getSize()/1024) + " kBytes. Current: " +
		    + Status.parseDouble(this.getSpeed()/1024) + "kB/s.")); //\n" + this.getTimeLeftMessage());
	    }
	    else {
		this.callObservers(new Message(Message.DATA, Status.parseDouble(this.getCurrent()/1024) + " of "
		    + Status.parseDouble(this.getSize()/1024) + " kBytes. Current: "
		    + Status.parseDouble(this.getSpeed()/1024) + "kB/s, Overall: "
		    + Status.parseDouble(this.getOverall()/1024) + " kB/s."));// \n" + this.getTimeLeftMessage());
	    }
	}
 */
    }


    public static double parseDouble(double d) {
        //log.debug("Status.parseDouble(" + d + ")");
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
    

    public void setResume(boolean resume) {
	this.resume = resume;
	if(!resume)
	    this.setCurrent(0);
    }
    
    public boolean isResume() {
	return this.resume;
    }

/*
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
*/

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
//}