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

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Timer;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Observable;
import java.util.Vector;
import java.net.URL;
import org.apache.log4j.Logger;
import ch.cyberduck.core.Preferences;

/**
  * The Status class is the model of a download's status. The view of this is represented by
  * the <code>StatusPanel</code> notified by the methods available from the <code>Observable</code> class.
  * To get notifed of my status register via <code>registerObserver()</code> of <code>BookmarkPanel</code>.
  * @version $Id$
  */
public class Status extends Observable implements Serializable {

    private static Logger log = Logger.getLogger(Status.class);

    /**
    * Progress trackers.
     */
    private transient Timer chronoTimer;

    private boolean canceled;
    /**
	* Indicates that the last action has been completed.
     */
    private boolean complete = false;
    /**
	* The last actioin has been stopped, but must not be completed.
     */
    private transient boolean stopped = true;

    /**
	* I am selected in the table view
     */
    /*
     private boolean selected;

     public void setSelected(boolean s) {
	 this.selected = s;
     }
     public boolean isSelected() {
	 return this.selected;
     }
     */


    private String progressmessage = "Idle";
    private String timemessage = "00:00";

    private Calendar calendar = Calendar.getInstance();
    private DateFormat df = DateFormat.getTimeInstance();
    
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;

    //@todo
//    private final static transient AudioClip startSound = Applet.newAudioClip(Cyberduck.getResource("start.au"));
//    private final static transient AudioClip stopSound = Applet.newAudioClip(Cyberduck.getResource("stop.au"));
//    private final static transient AudioClip completeSound = Applet.newAudioClip(Cyberduck.getResource("complete.au"));

    /**
	* The wrapper for any status informations of a transfer like it's length and transferred
     * bytes.
     */
    public Status () {
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        /*
	 timeLeftTimer = new Timer(1000,
			    new ActionListener() {
				public void actionPerformed(ActionEvent e) {
				    setTimeLeft((int)((getLength() - getCurrent())/getSpeed()));
				}
			    }
			    );
         */
    }

    /**
        * Notify all observers
     * @param arg The message to send to the observers
     * @see ch.cyberduck.core.Message
     */
    public void callObservers(Object arg) {
	//        log.debug("[Bookmark] Notifying " + this.countObservers() + " observers.");
 //        long start = System.currentTimeMillis();
        this.setChanged();
	//@todo        if(this.isSelected())
	this.notifyObservers(arg);
    }

    public void setMessage(String message, String title) {
//	log.debug("setMessage("+message+","+title);
	Message msg = new Message(title, message);
	if(title.equals(Message.PROGRESS)) {
	    this.progressmessage = message;
	    msg = new Message(Message.PROGRESS, timemessage+" "+message);
	}
	if(title.equals(Message.TIME)) {
	    this.timemessage = message;
	    msg = new Message(Message.PROGRESS, message+" "+progressmessage);
	}
        this.callObservers(msg);
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
    }
    public boolean isStopped() {
        return this.stopped;
    } 

    public void setCanceled(boolean b) {
        canceled = b;
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
        this.chronoTimer.start();
	this.callObservers(new Message(Message.ACTIVE, null));
    }

    /**
	* Notify that the connection has been closed.
     */
    public void fireStopEvent() {
        log.debug("fireStopEvent()");
        if(this.chronoTimer != null)
            this.chronoTimer.stop();
	this.setStopped(true);
	this.callObservers(new Message(Message.STOP, null));
    }

/**
* Notify that the transfer has been completed.
     */
    public void fireCompleteEvent() {
        log.debug("fireCompleteEvent()");
        this.chronoTimer.stop();
	this.setStopped(true);
	this.setComplete(true);
	this.callObservers(new Message(Message.COMPLETE, null));
    }

 /*
    private transient boolean ignoreEvents = false;
    
    public void ignoreEvents(boolean ignore) {
        this.ignoreEvents = ignore;
    }
  */
            

    /*
    private void setTimeLeft(int seconds) {
        this.left = seconds;
    }
    private int getTimeLeft() {
        return this.left;
    }
     */
    /*
    private String getTimeLeftMessage() {
        int s = this.getTimeLeft();
        String message = "";
        /*@todo: implementation of better 'time left' management.
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


    /*
    public String getPanelProperty() {
        if(this.panelProperty == null)
            this.panelProperty = this.getDefaultPanelProperty();
        return this.panelProperty;
    }

    public String getLastPanelProperty() {
        if(this.lastPanelProperty == null || this.panelProperty.equals(this.lastPanelProperty))
            return this.getDefaultPanelProperty();
        return lastPanelProperty;
    }

    public String getDefaultPanelProperty() {
        return PROGRESSPANEL;
    }
*/

    // initial panel property
    /*
    private String panelProperty;
    private String lastPanelProperty;
    
    public void setPanelProperty(String newPanelProperty) {
        log.debug("setPanelProperty("+newPanelProperty+")");
        if(lastPanelProperty != newPanelProperty) {
            this.lastPanelProperty = this.panelProperty;
        }
        if(panelProperty != newPanelProperty) {
            this.panelProperty = newPanelProperty;
            this.callObservers(newPanelProperty);
        }
    }
     */

    /**
     * reset messages and timers
     */
    public void reset() {
        log.debug("reset()");
        this.timemessage = "00:00";

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

                    // der variable timemessage den neuen wert zuweisen.
                    if(calendar.get(Calendar.HOUR) > 0) {
                        setMessage(parseTime(calendar.get(Calendar.HOUR)) + ":" + parseTime(calendar.get(Calendar.MINUTE)) + ":" + parseTime(calendar.get(Calendar.SECOND)), Message.TIME);
                    }
                    else {
                        setMessage(parseTime(calendar.get(Calendar.MINUTE)) + ":" + parseTime(calendar.get(Calendar.SECOND)), Message.TIME);
                    }
                }
            }
            );
        }
    }
} 
